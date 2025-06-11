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
import com.example.projectmanagement.utils.ParseDateUtil;
import com.example.projectmanagement.ui.helper.PhaseDragListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

//        project = getIntent().getParcelableExtra("project");
        // dùng project holder:
        project = ProjectHolder.get();
        if (project == null) {
            Toast.makeText(this, "Không nhận được Project", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        setupToolbar();
        applyProjectBackground(project.getBackgroundImg());
        setupBoard();
    }

    private void bindViews() {
        ivBackground     = findViewById(R.id.ivProjectBackground);
        coordinatorLayout= findViewById(R.id.coordinatorLayout);
        toolbar          = findViewById(R.id.toolbar_project);
        rvBoard          = findViewById(R.id.rvPhase);
        fabZoom          = findViewById(R.id.fabZoom);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(project.getProjectName());
        toolbar.setNavigationIcon(R.drawable.ic_back);

        toolbar.setNavigationOnClickListener(v -> {
            if (inInputMode) exitInputMode(); else finish();
        });

        toolbar.setOnMenuItemClickListener(this::handleToolbarItem);
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
        initData();
        
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

    private void initData() {
        // 1. Sample comments
        List<Comment> comments = new ArrayList<>();
        comments.add(new Comment(1, "Hello mọi người!",    1, 123, new Date()));
        comments.add(new Comment(2, "Đã xong bước này.",   1, 123, new Date()));
        comments.add(new Comment(3, "Cần bổ sung thêm X.", 1, 123, new Date()));

        // 2. Sample files
        List<File> files = new ArrayList<>();
        files.add(new File(1,
                "Report.pdf",
                "content://com.example.projectmanagement/files/Report.pdf",
                2300L,
                "pdf",
                1,
                123));
        files.add(new File(2,
                "Note.txt",
                "content://com.example.projectmanagement/files/Note.txt",
                121L,
                "txt",
                1,
                123));

        // 3. Build phases
        phases = new ArrayList<>();  // Reinitialize phases list
        int phaseID   = 1;
        int projectID = project.getProjectID();  // hoặc 1 nếu chưa có getter
        Date now      = new Date();

        for (int i = 1; i <= 3; i++, phaseID++) {
            // – Tạo 1 task mẫu cho mỗi phase
            List<Task> tasks = new ArrayList<>();
            int taskID = i;
            Task t = new Task(
                    /* taskID */        taskID,
                    /* phaseID */       phaseID,
                    /* taskName */      "Task " + taskID,
                    /* taskDescription*/"Mô tả cho task " + taskID,
                    /* assignedTo */    123,
                    /* status */        "WORKING",
                    /* priority */      "MEDIUM",
                    /* dueDate */       now,
                    /* allowSelfAssign*/ true,
                    /* orderIndex */    0,
                    /* createAt */      now,
                    /* lastUpdate */    now,
                    /* comments */      new ArrayList<>(comments),
                    /* files */         new ArrayList<>(files)
            );
            tasks.add(t);

            // – Tạo phase với constructor mới
            Phase p = new Phase(
                    /* phaseID */    phaseID,
                    /* projectID */  projectID,
                    /* phaseName */  "Ds" + phaseID,
                    /* description */"Mô tả phase " + phaseID,
                    /* orderIndex */ phaseID - 1,
                    /* createAt */   now,
                    /* tasks */      tasks
            );
            phases.add(p);
        }
        
        // Debug log
        android.util.Log.d("ProjectActivity", "initData: Created " + phases.size() + " phases");
        for (Phase phase : phases) {
            android.util.Log.d("ProjectActivity", "Phase: " + phase.getPhaseName() + " with " + phase.getTasks().size() + " tasks");
        }

        // 4. Gán lên project và lưu vào holder
        project.setPhases(phases);
        ProjectHolder.set(project);
    }


    private void applyProjectBackground(String bgImg) {
        if (bgImg == null) return;
        if (bgImg.startsWith("COLOR;")) {
            int color = Color.parseColor(bgImg.substring(6));
            ivBackground.setVisibility(View.GONE);
            coordinatorLayout.setBackgroundColor(color);
            toolbar.setBackgroundColor(color);
        } else if (bgImg.startsWith("GRADIENT;")) {
            String[] parts = bgImg.substring("GRADIENT;".length()).split(";");
            String[] cols  = parts[0].split(",");
            int ori        = Integer.parseInt(parts[1]);
            GradientDrawable gd = new GradientDrawable(
                    GradientDrawable.Orientation.values()[ori],
                    new int[]{ Color.parseColor(cols[0]), Color.parseColor(cols[1]) }
            );
            gd.setCornerRadius(0f);

            // 2. Hiển thị gradient full‐screen qua ImageView nền
            ivBackground.setVisibility(View.VISIBLE);
            ivBackground.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ivBackground.setImageDrawable(gd);

            // 3. Coi coordinatorLayout trong suốt để lộ gradient
            coordinatorLayout.setBackgroundColor(Color.TRANSPARENT);

            // 4. Giữ gradient trên toolbar để header hòa quyện
            toolbar.setBackground(gd);
        } else {
            // IMAGE cases
            ivBackground.setVisibility(View.VISIBLE);
            coordinatorLayout.setBackgroundColor(Color.TRANSPARENT);
            toolbar.setBackgroundColor(Color.TRANSPARENT);
            String source = bgImg.substring(bgImg.indexOf(';') + 1);
            Object model = bgImg.startsWith("URI;") ? Uri.parse(source) : Integer.parseInt(source);

            Glide.with(this)
                    .load(model)
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .listener(new RequestListener<Drawable>() {
                        @Override public boolean onLoadFailed(@Nullable GlideException e, Object m, Target<Drawable> t, boolean i) { return false; }
                        @Override public boolean onResourceReady(Drawable res, Object m, Target<Drawable> t, DataSource ds, boolean i) {
                            ivBackground.post(() -> {
                                ivBackground.setScaleType(ImageView.ScaleType.MATRIX);
                                ivBackground.setImageDrawable(res);
                                int vw = ivBackground.getWidth(), vh = ivBackground.getHeight();
                                int iw = res.getIntrinsicWidth(), ih = res.getIntrinsicHeight();
                                float scale = Math.max((float)vw/iw, (float)vh/ih);
                                float dx = (vw - iw*scale)/2f, dy = (vh - ih*scale)/2f;
                                Matrix mtx = new Matrix(); mtx.setScale(scale, scale); mtx.postTranslate(dx, dy);
                                ivBackground.setImageMatrix(mtx);
                            });
                            return true;
                        }
                    })
                    .into(ivBackground);
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
