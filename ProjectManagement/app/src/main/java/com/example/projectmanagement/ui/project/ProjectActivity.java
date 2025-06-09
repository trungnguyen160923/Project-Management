package com.example.projectmanagement.ui.project;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProjectActivity  extends AppCompatActivity implements PhaseAdapter.OnAddPhaseListener,
        PhaseAdapter.OnAddTaskListener, PhaseAdapter.OnTaskActionListener, PhaseAdapter.OnCardDropListener {

    private RecyclerView rvBoard;

    private Project project;
    private List<Phase> phases;
    private PhaseAdapter phaseAdapter;
    private FloatingActionButton fabZoom;

    // Toolbar UI
    private LinearLayout toolbarInputMode, toolbar_project_detail;
    private ImageButton btnCancelAdd, btnConfirmAdd, back_btn;

    private TextView toolbar_title_project;

    private int pendingPhase = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        project = (Project) getIntent().getParcelableExtra("project");
        if (project != null) {
            rvBoard = findViewById(R.id.rvPhase);
            rvBoard.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            initData();
            phaseAdapter = new PhaseAdapter(phases, this, this, this, this, rvBoard);
            rvBoard.setAdapter(phaseAdapter);

            // 2) Ánh xạ Toolbar
            toolbarInputMode = findViewById(R.id.toolbar_input_mode);
            toolbar_project_detail = findViewById(R.id.toolbar_project_detail);
            btnCancelAdd     = findViewById(R.id.btn_cancel_add);
            btnConfirmAdd    = findViewById(R.id.btn_confirm_add);
            toolbar_title_project = findViewById(R.id.toolbar_title_project);
            back_btn = findViewById(R.id.btn_project_back);

            back_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            toolbar_title_project.setText(project.getProjectName());

            btnConfirmAdd.setEnabled(false);
            btnConfirmAdd.setAlpha(0.3f);

            // 3) Xử lý X (hủy)
            btnCancelAdd.setOnClickListener(v -> {
                phaseAdapter.cancelEditing();
                exitAddMode();
            });

            // 4) Xử lý ✓ (xác nhận)
            btnConfirmAdd.setOnClickListener(v -> {
                if (pendingPhase >= 0) {
                    String name = phaseAdapter.getNewTaskName();
                    if (!name.isEmpty()) {
                        onTaskAddConfirmed(pendingPhase, name);
                    }
                }
                phaseAdapter.cancelEditing();
                exitAddMode();
            });

            int scrollThreshold = 100;  // khi kéo vào 100px gần mép thì scroll
            int scrollAmount    = 100;   // mỗi lần scroll 20px
            PhaseDragListener.OnCardDropListener dropListener =
                    new PhaseDragListener.OnCardDropListener() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onCardDropped(Phase targetPhase,
                                                  int dropIndex,
                                                  DraggedTaskInfo info) {

                            List<Task> sourceList = info.getPhase().getTasks();
                            List<Task> destList   = targetPhase.getTasks();
                            Task task             = info.getTask();
                            int originalPos       = info.getOriginalPosition();

                            // 1) Remove theo object để không bị OOB
                            boolean removed = sourceList.remove(task);

                            // 2) Nếu kéo trong cùng phase, cần điều chỉnh dropIndex
                            if (removed && targetPhase == info.getPhase()) {
                                // Nếu dropIndex > originalPos thì sau remove, vị trí thực tế giảm 1
                                if (dropIndex > originalPos) {
                                    dropIndex--;
                                }
                            }

                            // 3) Clamp dropIndex nằm trong [0 .. destList.size()]
                            dropIndex = Math.max(0, Math.min(dropIndex, destList.size()));

                            // 4) Thêm vào destList
                            destList.add(dropIndex, task);

                            // 5) Cập nhật UI
                            phaseAdapter.notifyDataSetChanged();
                        }
                    };
            PhaseDragListener dragListener = new PhaseDragListener(
                    rvBoard,
                    phases,
                    dropListener,
                    scrollThreshold,
                    scrollAmount
            );

            // 5. Gắn listener cho rvBoard
            rvBoard.setOnDragListener(dragListener);
        } else {
            // Xử lý khi không có data
            Toast.makeText(this, "Không nhận được Project", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private void initData() {
        phases = new ArrayList<>();

        // Ds1
        List<Task> ds1Tasks = new ArrayList<>();
        List<Comment> comments = new ArrayList<>();
        comments.add(new Comment(1, "Hello mọi người!", 123, 10, new Date()));
        comments.add(new Comment(2, "Đã xong bước này.", 123, 11, new Date()));
        comments.add(new Comment(3, "Cần bổ sung thêm phần X.", 123, 12, new Date()));


        List<File> files = new ArrayList<>();
        files.add(new File(1,"Report.pdf","content://com.example.yourapp/files/Report.pdf",2300,"pdf",1,1));
        files.add(new File(2,"Note.txt","content://com.example.yourapp/files/Note.txt",121,"txt",1,1 ));
        ds1Tasks.add(new Task("Thẻ 1 ds1", "Mô tả", "DONE", "25/04/2025", comments, files));
        ds1Tasks.add(new Task("Thẻ 2 ds1", "Mô tả", "WORKING", "25/04/2025", comments, files));
        phases.add(new Phase("Ds1", ds1Tasks));

        // Ds2 (bỏ phần giờ, chỉ dd/MM/yyyy)
        List<Task> ds2Tasks = new ArrayList<>();
        ds2Tasks.add(new Task("Hehe", "Mô tả", "WORKING", "25/04/2025", comments, files));
        phases.add(new Phase("Ds2", ds2Tasks));

        // Ds3
        List<Task> ds3Tasks = new ArrayList<>();
        ds3Tasks.add(new Task("Hello 1", "Mô tả", "WORKING", "25/04/2025", comments, files));
        phases.add(new Phase("Ds3", ds3Tasks));

        project.setPhases(phases);
    }

    @Override
    public void onAddTask(int phasePosition) {
        List<Task> tasks = phases.get(phasePosition).getTasks();
        List<Comment> comments = new ArrayList<>();
        comments.add(new Comment(1, "Hello mọi người!", 123, 10, new Date()));
        comments.add(new Comment(2, "Đã xong bước này.", 123, 11, new Date()));
        comments.add(new Comment(3, "Cần bổ sung thêm phần X.", 123, 12, new Date()));


        List<File> files = new ArrayList<>();
        files.add(new File(1,"Report.pdf","content://com.example.yourapp/files/Report.pdf",2300,"pdf",1,1));
        files.add(new File(2,"Note.txt","content://com.example.yourapp/files/Note.txt",121,"txt",1,1 ));
        // Dùng ngày hôm nay làm due date
        String today = ParseDateUtil.formatDate(new Date());
        tasks.add(new Task("Task mới", "", "WORKING", today, comments, files));
        phaseAdapter.notifyItemChanged(phasePosition);
    }

    @Override
    public void onAddPhase() {
        List<Task> t = new ArrayList<>();
        String today = ParseDateUtil.formatDate(new Date());
        List<Comment> comments = new ArrayList<>();
        comments.add(new Comment(1, "Hello mọi người!", 123, 10, new Date()));
        comments.add(new Comment(2, "Đã xong bước này.", 123, 11, new Date()));
        comments.add(new Comment(3, "Cần bổ sung thêm phần X.", 123, 12, new Date()));


        List<File> files = new ArrayList<>();
        files.add(new File(1,"Report.pdf","content://com.example.yourapp/files/Report.pdf",2300,"pdf",1,1));
        files.add(new File(2,"Note.txt","content://com.example.yourapp/files/Note.txt",121,"txt",1,1 ));
        t.add(new Task("Task mới", "", "WORKING", today, comments, files));
        phases.add(new Phase("Ds" + (phases.size() + 1), t));
        phaseAdapter.notifyItemInserted(phases.size() - 1);
    }

    @Override
    public void onTaskAddRequested(int phasePosition) {
        pendingPhase = phasePosition;
        // bật chế độ input trên adapter
        phaseAdapter.startEditing(phasePosition);
        // bật toolbar input mode
        toolbarInputMode.setVisibility(View.VISIBLE);
        toolbar_project_detail.setVisibility(View.GONE);
    }

    @Override
    public void onTaskAddConfirmed(int phasePosition, String taskName) {
        List<Task> tasks = phases.get(phasePosition).getTasks();
        String today = ParseDateUtil.formatDate(new Date());
        List<Comment> comments = new ArrayList<>();
        comments.add(new Comment(1, "Hello mọi người!", 123, 10, new Date()));
        comments.add(new Comment(2, "Đã xong bước này.", 123, 11, new Date()));
        comments.add(new Comment(3, "Cần bổ sung thêm phần X.", 123, 12, new Date()));


        List<File> files = new ArrayList<>();
        files.add(new File(1,"Report.pdf","content://com.example.yourapp/files/Report.pdf",2300,"pdf",1,1));
        files.add(new File(2,"Note.txt","content://com.example.yourapp/files/Note.txt",121,"txt",1,1 ));
        tasks.add(new Task(taskName, "", "WORKING", today, comments, files));
        phaseAdapter.notifyItemChanged(phasePosition);

        // ẩn/hiện toolbar sau khi confirm
        toolbarInputMode.setVisibility(View.GONE);
        toolbar_project_detail.setVisibility(View.VISIBLE);
    }

    /** Khi bấm X trên toolbar */
    @Override
    public void onTaskAddCanceled() {
        // đã hủy trong btnCancelAdd => chỉ cần quay về mặc định
        exitAddMode();
    }

    @Override
    public void onTaskNameChanged(int phasePosition, String newName) {
        if (phasePosition == pendingPhase) {
            boolean ok = !newName.trim().isEmpty();
            btnConfirmAdd.setEnabled(ok);
            btnConfirmAdd.setAlpha(ok ? 1f : 0.3f);
        }
    }

    private void exitAddMode() {
        pendingPhase = -1;
        toolbarInputMode.setVisibility(View.GONE);
        toolbar_project_detail.setVisibility(View.VISIBLE);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onCardDropped(Phase targetList, int dropIndex, DraggedTaskInfo draggedInfo) {
        targetList.getTasks().add(dropIndex, draggedInfo.getTask());
        phaseAdapter.notifyDataSetChanged();
    }

}