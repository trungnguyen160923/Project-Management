package com.example.projectmanagement.ui.project;

import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.projectmanagement.data.model.Task;
import com.example.projectmanagement.ui.adapter.PhaseAdapter;
import com.example.projectmanagement.utils.ParseDateUtil;
import com.example.projectmanagement.utils.PhaseDragListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private List<Phase> phases;
    private PhaseAdapter phaseAdapter;

    private boolean inInputMode = false;
    private int pendingPhase = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        project = getIntent().getParcelableExtra("project");
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
        toolbar.setTitle(project.getProjectName());
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.inflateMenu(R.menu.menu_project_toolbar);

        toolbar.setNavigationOnClickListener(v -> {
            if (inInputMode) exitInputMode(); else finish();
        });

        toolbar.setOnMenuItemClickListener(this::handleToolbarItem);
    }

    private boolean handleToolbarItem(MenuItem item) {
        if (inInputMode && item.getItemId() == R.id.action_confirm_add) {
            if (pendingPhase >= 0) onTaskAddConfirmed(pendingPhase, phaseAdapter.getNewTaskName());
            exitInputMode();
            return true;
        }
        // handle sort, notifications, more here if needed
        return false;
    }

    private void setupBoard() {
        rvBoard.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        initData();
        phaseAdapter = new PhaseAdapter(phases, this, this, this, this, rvBoard);
        rvBoard.setAdapter(phaseAdapter);

        PhaseDragListener.OnCardDropListener dropListener = (target, dropIndex, info) -> {
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
    }

    private void initData() {
        // Sample data setup
        List<Comment> comments = new ArrayList<>();
        comments.add(new Comment(1, "Hello mọi người!", 123, 10, new Date()));
        comments.add(new Comment(2, "Đã xong bước này.", 123, 11, new Date()));
        comments.add(new Comment(3, "Cần bổ sung thêm phần X.", 123, 12, new Date()));

        List<File> files = new ArrayList<>();
        files.add(new File(1, "Report.pdf", "content://.../Report.pdf", 2300, "pdf", 1, 1));
        files.add(new File(2, "Note.txt", "content://.../Note.txt", 121, "txt", 1, 1));

        phases = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            List<Task> tasks = new ArrayList<>();
            tasks.add(new Task("Task " + i, "Mô tả", "WORKING", ParseDateUtil.formatDate(new Date()), comments, files));
            phases.add(new Phase("Ds" + i, tasks));
        }
        project.setPhases(phases);
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

        // 2. Thông báo adapter về vị trí chèn
        phaseAdapter.notifyItemInserted(phases.size() - 1);
    }
    @Override public void onAddTask(int pos) { onAddTaskGeneric(pos); phaseAdapter.notifyItemChanged(pos);}
    private void onAddTaskGeneric(int pos) {
        List<Task> tasks = phases.get(pos).getTasks();
        String today = ParseDateUtil.formatDate(new Date());
        tasks.add(new Task("Task mới", "", "WORKING", today, new ArrayList<>(), new ArrayList<>()));
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
    }
}
