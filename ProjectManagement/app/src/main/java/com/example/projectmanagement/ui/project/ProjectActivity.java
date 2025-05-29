package com.example.projectmanagement.ui.project;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.DraggedTaskInfo;
import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.data.model.Task;
import com.example.projectmanagement.ui.adapter.PhaseAdapter;
import com.example.projectmanagement.utils.PhaseDragListener;
import com.example.projectmanagement.utils.PhaseTouchHelperCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ProjectActivity  extends AppCompatActivity implements PhaseAdapter.OnAddPhaseListener,
        PhaseAdapter.OnAddTaskListener, PhaseAdapter.OnTaskActionListener, PhaseAdapter.OnCardDropListener {

    private RecyclerView rvBoard;
    private List<Phase> phases;
    private PhaseAdapter phaseAdapter;
    private FloatingActionButton fabZoom;

    // Toolbar UI
    private LinearLayout toolbarInputMode, toolbar_project_detail;
    private ImageButton btnCancelAdd, btnConfirmAdd;

    private int pendingPhase = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

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

        ItemTouchHelper helper = new ItemTouchHelper(
                new PhaseTouchHelperCallback(phaseAdapter)
        );
        helper.attachToRecyclerView(rvBoard);

        final int BOARD_SCROLL_THRESHOLD = 200;
        final int BOARD_SCROLL_AMOUNT  = 100;
        PhaseDragListener boardDragListener = new PhaseDragListener(
                rvBoard, phases, phaseAdapter,
                BOARD_SCROLL_THRESHOLD, BOARD_SCROLL_AMOUNT
        );
        rvBoard.setOnDragListener(boardDragListener);
    }

    private void initData() {
        phases = new ArrayList<>();

        // Ds1
        List<Task> ds1Tasks = new ArrayList<>();
        ds1Tasks.add(new Task("Thẻ 1 ds1", "Mo ta", "DONE", "25/04/2025", 0,0));
        ds1Tasks.add(new Task("Thẻ 2 ds1", "Mo ta", "WORKING", "25/04/2025", 2,3));
        phases.add(new Phase("Ds1", ds1Tasks));


        // Ds2
        List<Task> ds2Tasks = new ArrayList<>();
        ds2Tasks.add(new Task("Hehe", "Mo ta", "WORKING", "25/04/2025 12:00",  0,2));
        phases.add(new Phase("Ds2", ds2Tasks));

        // Ds3
        List<Task> ds3Tasks = new ArrayList<>();
        ds3Tasks.add(new Task("Hello 1", "Mo ta", "WORKING", "25/04/2025 12:00", 3,0));
        phases.add(new Phase("Ds3", ds3Tasks));
    }


    @Override
    public void onAddTask(int phasePosition) {
        List<Task> tasks = (List<Task>) phases.get(phasePosition).getTasks();
        tasks.add(new Task("Task mới ", "", "WORKING", "", 0,0));
        phaseAdapter.notifyItemChanged(phasePosition);
    }

    /** Thêm phase mới */
    @Override
    public void onAddPhase() {
        List<Task> t = new ArrayList<>();
        t.add(new Task("Task mới", "", "WORKING", "", 0,0));
        phases.add(new Phase("Ds" + (phases.size()+1), t));
        phaseAdapter.notifyItemInserted(phases.size()-1);
    }

    /** Khi adapter phát đi “bấm +Thêm thẻ” */
    @Override
    public void onTaskAddRequested(int phasePosition) {
        pendingPhase = phasePosition;
        // bật chế độ input trên adapter
        phaseAdapter.startEditing(phasePosition);
        // bật toolbar input mode
        toolbarInputMode.setVisibility(View.VISIBLE);
        toolbar_project_detail.setVisibility(View.GONE);
    }

    /** Khi bấm ✓ trên toolbar */
    @Override
    public void onTaskAddConfirmed(int phasePosition, String taskName) {
        List<Task> tasks = (List<Task>) phases.get(phasePosition).getTasks();
        tasks.add(new Task(taskName, "", "WORKING", "", 0, 0));
        phaseAdapter.notifyItemChanged(phasePosition);
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