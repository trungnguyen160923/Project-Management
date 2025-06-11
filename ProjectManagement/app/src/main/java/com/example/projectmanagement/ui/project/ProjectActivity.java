package com.example.projectmanagement.ui.project;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.Comment;
import com.example.projectmanagement.data.model.DraggedTaskInfo;
import com.example.projectmanagement.data.model.File;
import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.data.model.ProjectHolder;
import com.example.projectmanagement.data.model.Task;
import com.example.projectmanagement.ui.adapter.PhaseAdapter;
import com.example.projectmanagement.ui.helper.PhaseOrderTouchCallback;
import com.example.projectmanagement.ui.notification.NotificationActivity;
import com.example.projectmanagement.ui.project.vm.ProjectViewModel;
import com.example.projectmanagement.utils.ParseDateUtil;
import com.example.projectmanagement.ui.helper.PhaseDragListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ProjectActivity extends AppCompatActivity implements
        PhaseAdapter.OnAddPhaseListener,
        PhaseAdapter.OnAddTaskListener,
        PhaseAdapter.OnTaskActionListener,
        PhaseAdapter.OnCardDropListener {

    private RecyclerView rvBoard;
    private ImageView ivBackground;
    private CoordinatorLayout coordinatorLayout;
    private MaterialToolbar toolbar;
    private FloatingActionButton fabZoom;

    private Project project;
    private List<Phase> phases = new ArrayList<>();
    private PhaseAdapter phaseAdapter;

    private boolean inInputMode = false;
    private int pendingPhase = -1;

    private ProjectViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        // Lấy project từ ProjectHolder
        project = ProjectHolder.get();
        if (project == null) {
            Toast.makeText(this, "Không nhận được Project", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        setupInitialToolbar();
        setupBoard();
        applyProjectBackground(project.getBackgroundImg());
    }

    private void bindViews() {
        ivBackground     = findViewById(R.id.ivProjectBackground);
        coordinatorLayout= findViewById(R.id.coordinatorLayout);
        toolbar          = findViewById(R.id.toolbar_project);
        rvBoard          = findViewById(R.id.rvPhase);
        fabZoom          = findViewById(R.id.fabZoom);
    }

    private void setupInitialToolbar() {
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(project.getProjectName());
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> {
            if (inInputMode) exitInputMode(); else finish();
        });
        toolbar.setOnMenuItemClickListener(this::handleToolbarItem);
        toolbar.inflateMenu(R.menu.menu_project_toolbar);
    }

    private boolean handleToolbarItem(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            showSearchView();
            return true;
        } else if (id == R.id.action_notifications) {
            startActivity(new Intent(this, NotificationActivity.class));
            return true;
        } else if (id == R.id.action_more) {
            startActivity(new Intent(this, MenuProjectActivity.class));
            return true;
        }else if (id == R.id.action_confirm_add) {
            String name = phaseAdapter.getNewTaskName();
            if (name == null || name.trim().isEmpty()) {
                Toast.makeText(this, "Tên thẻ không được để trống!", Toast.LENGTH_SHORT).show();
                return true;
            }

            onTaskAddConfirmed(pendingPhase, name);
            return true;
        }
        return false;
    }

    private void showSearchView() {
        // Ẩn tiêu đề
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Tạo EditText tùy chỉnh
        EditText searchEditText = new EditText(this);
        searchEditText.setHint("Tìm kiếm...");
        searchEditText.setSingleLine(true);
        searchEditText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchEditText.setBackgroundColor(Color.TRANSPARENT);
        searchEditText.setTextColor(Color.WHITE);
        searchEditText.setHintTextColor(Color.WHITE);

        // Thiết lập kích thước và vị trí
        Toolbar.LayoutParams params = new Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                getResources().getDimensionPixelSize(R.dimen.search_view_height)
        );
        params.gravity = android.view.Gravity.START | android.view.Gravity.CENTER_VERTICAL;
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.toolbar_content_inset);
        searchEditText.setLayoutParams(params);

        // Thêm padding
        int horizontalPadding = getResources().getDimensionPixelSize(R.dimen.search_view_padding);
        int verticalPadding = getResources().getDimensionPixelSize(R.dimen.search_view_padding_vertical);
        searchEditText.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);

        // Thêm vào toolbar
        toolbar.addView(searchEditText);

        // Xử lý sự kiện tìm kiếm
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // Xử lý tìm kiếm
                return true;
            }
            return false;
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Xử lý khi text thay đổi
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });

        // Xử lý khi đóng
        searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                toolbar.removeView(searchEditText);
                getSupportActionBar().setDisplayShowTitleEnabled(true);
            }
        });

        // Hiển thị keyboard
        searchEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void setupBoard() {
        rvBoard.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Debug log
        android.util.Log.d("ProjectActivity", "setupBoard: phases size = " + (phases != null ? phases.size() : "null"));

        phaseAdapter = new PhaseAdapter(phases, this, this, this, this, rvBoard);
        rvBoard.setAdapter(phaseAdapter);

        // Force adapter to refresh
        phaseAdapter.notifyDataSetChanged();

        // Debug log
        android.util.Log.d("ProjectActivity", "setupBoard: Adapter created and set");

        @SuppressLint("NotifyDataSetChanged") PhaseDragListener.OnCardDropListener dropListener = (target, dropIndex, info) -> {
            List<Task> src = info.getPhase().getTasks();
            List<Task> dst = target.getTasks();
            Task task = info.getTask();
            int orig = info.getOriginalPosition();

            if (src.remove(task) && target == info.getPhase() && dropIndex > orig) dropIndex--;
            dropIndex = Math.max(0, Math.min(dropIndex, dst.size()));
            dst.add(dropIndex, task);
            phaseAdapter.notifyDataSetChanged();
        };

        PhaseDragListener dragListener = new PhaseDragListener(rvBoard, phases, dropListener, 100, 100);
        rvBoard.setOnDragListener(dragListener);

        // Add phase reordering support
        ItemTouchHelper.Callback phaseOrderCallback = new PhaseOrderTouchCallback(phases, phaseAdapter);
        ItemTouchHelper phaseTouchHelper = new ItemTouchHelper(phaseOrderCallback);
        phaseTouchHelper.attachToRecyclerView(rvBoard);
    }

    private void applyProjectBackground(String bgImg) {
        if (bgImg == null || bgImg.isEmpty()) {
            // Set default background if no image is provided
            ivBackground.setVisibility(View.GONE);
            coordinatorLayout.setBackgroundColor(Color.parseColor("#0C90F1")); // Default blue color
            toolbar.setBackgroundColor(Color.parseColor("#0C90F1"));
            return;
        }

        if (bgImg.startsWith("COLOR;")) {
            try {
                int color = Color.parseColor(bgImg.substring(6));
                ivBackground.setVisibility(View.GONE);
                coordinatorLayout.setBackgroundColor(color);
                toolbar.setBackgroundColor(color);
            } catch (Exception e) {
                Log.e("ProjectActivity", "Error parsing color: " + e.getMessage());
                // Set default color on error
                coordinatorLayout.setBackgroundColor(Color.parseColor("#0C90F1"));
                toolbar.setBackgroundColor(Color.parseColor("#0C90F1"));
            }
        } else if (bgImg.startsWith("GRADIENT;")) {
            try {
                String[] parts = bgImg.substring("GRADIENT;".length()).split(";");
                String[] cols = parts[0].split(",");
                int ori = Integer.parseInt(parts[1]);
                GradientDrawable gd = new GradientDrawable(
                        GradientDrawable.Orientation.values()[ori],
                        new int[]{Color.parseColor(cols[0]), Color.parseColor(cols[1])}
                );
                gd.setCornerRadius(0f);

                ivBackground.setVisibility(View.VISIBLE);
                ivBackground.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ivBackground.setImageDrawable(gd);
                coordinatorLayout.setBackgroundColor(Color.TRANSPARENT);
                toolbar.setBackground(gd);
            } catch (Exception e) {
                Log.e("ProjectActivity", "Error parsing gradient: " + e.getMessage());
                // Set default gradient on error
                GradientDrawable gd = new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{Color.parseColor("#0C90F1"), Color.parseColor("#0A7BC8")}
                );
                ivBackground.setImageDrawable(gd);
                toolbar.setBackground(gd);
            }
        } else {
            // IMAGE cases
            ivBackground.setVisibility(View.VISIBLE);
            coordinatorLayout.setBackgroundColor(Color.TRANSPARENT);
            toolbar.setBackgroundColor(Color.TRANSPARENT);
            
            try {
                String source = bgImg.substring(bgImg.indexOf(';') + 1);
                Object model = bgImg.startsWith("URI;") ? Uri.parse(source) : Integer.parseInt(source);

                Glide.with(this)
                        .load(model)
                        .format(DecodeFormat.PREFER_ARGB_8888)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object m, Target<Drawable> t, boolean i) {
                                Log.e("ProjectActivity", "Error loading image: " + e.getMessage());
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable res, Object m, Target<Drawable> t, DataSource ds, boolean i) {
                                ivBackground.post(() -> {
                                    ivBackground.setScaleType(ImageView.ScaleType.MATRIX);
                                    ivBackground.setImageDrawable(res);
                                    int vw = ivBackground.getWidth(), vh = ivBackground.getHeight();
                                    int iw = res.getIntrinsicWidth(), ih = res.getIntrinsicHeight();
                                    float scale = Math.max((float) vw / iw, (float) vh / ih);
                                    float dx = (vw - iw * scale) / 2f, dy = (vh - ih * scale) / 2f;
                                    Matrix mtx = new Matrix();
                                    mtx.setScale(scale, scale);
                                    mtx.postTranslate(dx, dy);
                                    ivBackground.setImageMatrix(mtx);
                                });
                                return true;
                            }
                        })
                        .into(ivBackground);
            } catch (Exception e) {
                Log.e("ProjectActivity", "Error loading image: " + e.getMessage());
                // Set default background on error
                ivBackground.setVisibility(View.GONE);
                coordinatorLayout.setBackgroundColor(Color.parseColor("#0C90F1"));
                toolbar.setBackgroundColor(Color.parseColor("#0C90F1"));
            }
        }
    }

    private void enterInputMode() {
        inInputMode = true;
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.menu_input_mode);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.setTitle("Thêm thẻ…");
    }

    private void exitInputMode() {
        inInputMode = false;
        pendingPhase = -1;
        phaseAdapter.cancelEditing();
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.menu_project_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setTitle(project.getProjectName());
    }

    @Override public void onAddPhase() {
        List<Task> tasks = new ArrayList<>();
        String today = ParseDateUtil.formatDate(new Date());
        // ... tạo comments, files, task mẫu như bạn đã làm trước đó
        phases.add(new Phase("Ds" + (phases.size() + 1), tasks));
        project.setPhases(phases);
        ProjectHolder.set(project);

        // 2. Thông báo adapter về vị trí chèn
        phaseAdapter.notifyItemInserted(phases.size() - 1);
    }
    @Override public void onAddTask(int pos) { onAddTaskGeneric(pos); phaseAdapter.notifyItemChanged(pos);}
    private void onAddTaskGeneric(int pos) {
        List<Task> tasks = phases.get(pos).getTasks();
        String today = ParseDateUtil.formatDate(new Date());
        tasks.add(new Task("Task mới", "", "WORKING", today, new ArrayList<>(), new ArrayList<>()));
        ProjectHolder.set(project);
    }


    @Override public void onTaskAddRequested(int pos) { pendingPhase=pos; phaseAdapter.startEditing(pos); enterInputMode(); }
    @Override public void onTaskAddConfirmed(int pos,String name) { onAddTaskGeneric(pos); exitInputMode(); }
    @Override public void onTaskAddCanceled() { exitInputMode(); }
    @Override public void onTaskNameChanged(int pos,String name) {
        if (pos==pendingPhase) {
            MenuItem item=toolbar.getMenu().findItem(R.id.action_confirm_add);
            item.setEnabled(!name.trim().isEmpty());
        }
    }
    @Override public void onCardDropped(Phase target,int idx,DraggedTaskInfo info) {
        List<Task> src=info.getPhase().getTasks(), dst=target.getTasks();
        Task t=info.getTask(); int orig=info.getOriginalPosition();
        if(src.remove(t)&& target==info.getPhase()&& idx>orig) idx--;
        idx=Math.max(0,Math.min(idx,dst.size())); dst.add(idx,t); phaseAdapter.notifyDataSetChanged();
        ProjectHolder.set(project);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_project_toolbar, menu);
        return true;
    }
}