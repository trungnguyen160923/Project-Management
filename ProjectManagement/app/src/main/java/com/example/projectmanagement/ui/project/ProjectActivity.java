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
import android.os.Handler;
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
import com.example.projectmanagement.data.service.TaskService;
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

    private boolean isAddingPhase = false;
    private long lastClickTime = 0;
    private static final long CLICK_DELAY = 1000; // 1 second delay

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

        // Debug log project data
        Log.d("ProjectActivity", "Project data: " + 
            "id=" + project.getProjectID() + 
            ", name=" + project.getProjectName() + 
            ", description=" + project.getProjectDescription() +
            ", phases=" + (project.getPhases() != null ? project.getPhases().size() : 0));

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(ProjectViewModel.class);
        viewModel.init(this);
        viewModel.setProject(project);

        bindViews();
        setupInitialToolbar();
        setupBoard();
        applyProjectBackground(project.getBackgroundImg());

        // Observe project data changes
        viewModel.getProject().observe(this, updatedProject -> {
            if (updatedProject != null) {
                project = updatedProject;
                toolbar.setTitle(project.getProjectName());
                Log.d("ProjectActivity", "Project updated: " + project.getProjectName());
            }
        });

        // Observe phases data changes
        viewModel.getPhases().observe(this, updatedPhases -> {
            if (updatedPhases != null) {
                phases = updatedPhases;
                Log.d("ProjectActivity", "Phases updated: " + phases.size() + " phases");
                phaseAdapter.updatePhases(phases);
                // Force refresh UI
                phaseAdapter.notifyDataSetChanged();
            }
        });

        // Observe messages
        viewModel.getMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật project từ ProjectHolder khi activity resume
        Project currentProject = ProjectHolder.get();
        if (currentProject != null) {
            viewModel.setProject(currentProject);
        }
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
        Log.d("ProjectActivity", "setupBoard: phases size = " + (phases != null ? phases.size() : "null"));

        // Ensure phases are loaded from project
        if (project != null && project.getPhases() != null) {
            phases = new ArrayList<>(project.getPhases());
            Log.d("ProjectActivity", "Loaded " + phases.size() + " phases from project");
        }

        phaseAdapter = new PhaseAdapter(phases, this, this, this, this, rvBoard);
        rvBoard.setAdapter(phaseAdapter);

        // Force adapter to refresh
        phaseAdapter.notifyDataSetChanged();

        // Debug log
        Log.d("ProjectActivity", "setupBoard: Adapter created and set with " + phases.size() + " phases");

        // Create an adapter to convert between the two OnCardDropListener interfaces
        PhaseDragListener.OnCardDropListener dropListener = (target, dropIndex, info) -> {
            // Call the activity's onCardDropped method
            onCardDropped(target, dropIndex, info);
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

    @Override 
    public void onAddPhase() {
        // Prevent multiple clicks
        long currentTime = System.currentTimeMillis();
        if (isAddingPhase || (currentTime - lastClickTime) < CLICK_DELAY) {
            Log.d("ProjectActivity", "Ignoring click - too soon or already adding phase");
            return;
        }

        lastClickTime = currentTime;
        isAddingPhase = true;

        // Debug log project data before creating phase
        Log.d("ProjectActivity", "Creating phase with project: " + 
            "id=" + project.getProjectID() + 
            ", name=" + project.getProjectName());

        // Call ViewModel to add phase
        viewModel.addPhase(phases.size());
        
        // Observe message for feedback
        viewModel.getMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                // Reset flag after a short delay
                new Handler().postDelayed(() -> isAddingPhase = false, 500);
            }
        });
    }
    @Override public void onAddTask(int pos) { 
        onAddTaskGeneric(pos); 
        phaseAdapter.notifyItemChanged(pos);
    }
    @SuppressLint("NotifyDataSetChanged")
    private void onAddTaskGeneric(int pos) {
        // Lấy tên task từ adapter
        String taskName = phaseAdapter.getNewTaskName();
        if (taskName == null || taskName.trim().isEmpty()) {
            Toast.makeText(this, "Tên task không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        // Call ViewModel to add task với tên đã nhập
        viewModel.addTask(pos, taskName);
        
        // Observe message for feedback
        viewModel.getMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        // Cập nhật ProjectHolder và UI
        Project currentProject = ProjectHolder.get();
        if (currentProject != null) {
            // Cập nhật ProjectHolder
            ProjectHolder.set(currentProject);
            
            // Cập nhật UI thông qua ViewModel
            viewModel.setProject(currentProject);
            
            // Force refresh adapter
            phaseAdapter.notifyDataSetChanged();
        }
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
    @Override public void onCardDropped(Phase target, int idx, DraggedTaskInfo info) {
        Log.d("ProjectActivity", "onCardDropped called - Task: " + info.getTask().getTaskName() + 
            ", From Phase: " + info.getPhase().getPhaseName() + 
            ", To Phase: " + target.getPhaseName() + 
            ", Position: " + idx);

        List<Task> src = info.getPhase().getTasks();
        List<Task> dst = target.getTasks();
        Task task = info.getTask();
        int orig = info.getOriginalPosition();

        // Update UI first
        if (src.remove(task) && target == info.getPhase() && idx > orig) idx--;
        idx = Math.max(0, Math.min(idx, dst.size()));
        dst.add(idx, task);
        phaseAdapter.notifyDataSetChanged();

        // Make index final for lambda
        final int finalIdx = idx;

        Log.d("ProjectActivity", "Calling moveTask API - TaskId: " + task.getTaskID() + 
            ", PhaseId: " + target.getPhaseID() + 
            ", Position: " + finalIdx + 
            ", ProjectId: " + project.getProjectID());

        // Call API to update task position
        TaskService.moveTask(
            this,
            task.getTaskID(),
            target.getPhaseID(),
            finalIdx,
            project.getProjectID(),
            response -> {
                Log.d("ProjectActivity", "Task moved successfully - Response: " + response.toString());
                // Update project in holder
                ProjectHolder.set(project);
            },
            error -> {
                Log.e("ProjectActivity", "Error moving task: " + error.getMessage());
                // Revert changes if API call fails
                dst.remove(finalIdx);
                src.add(orig, task);
                phaseAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Failed to move task. Please try again.", Toast.LENGTH_SHORT).show();
            }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_project_toolbar, menu);
        return true;
    }
}