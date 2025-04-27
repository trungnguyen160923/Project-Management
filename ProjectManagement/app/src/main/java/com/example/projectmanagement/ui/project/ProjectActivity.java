package com.example.projectmanagement.ui.project;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.DraggedTaskInfo;
import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.data.model.Task;
import com.example.projectmanagement.ui.adapter.PhaseAdapter;
import com.example.projectmanagement.ui.adapter.TaskAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProjectActivity extends AppCompatActivity
        implements PhaseAdapter.OnAddPhaseListener,
        PhaseAdapter.OnTaskActionListener,
        TaskAdapter.OnTaskDropListener {

    private List<Phase> phaseList;
    private PhaseAdapter phaseAdapter;
    private LinearLayout toolbarInput, toolbarDetail;
    private ImageButton btnCancel, btnConfirm;
    private int editingPhase = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        bindViews();
        loadData();
        setupRecycler();
        setupToolbar();
    }

    private void bindViews() {
        toolbarInput  = findViewById(R.id.toolbar_input_mode);
        toolbarDetail = findViewById(R.id.toolbar_project_detail);
        btnCancel     = findViewById(R.id.btn_cancel_add);
        btnConfirm    = findViewById(R.id.btn_confirm_add);
    }

    private void loadData() {
        phaseList = new ArrayList<>();
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");

        try {
            List<Task> t1 = new ArrayList<>();
            t1.add(new Task(1, 1, "Thẻ 1 ds1", "Mô tả", 0, "DONE", "HIGH", fmt.parse("25/04/2025"), true, 0, new Date(), null, 0, 0));
            t1.add(new Task(2, 1, "Thẻ 2 ds1", "Mô tả", 2, "WORKING", "MEDIUM", fmt.parse("25/04/2025"), true, 1, new Date(), null, 2, 3));
            phaseList.add(new Phase(1, 1, "Ds1", "", 0, new Date(), t1));

            List<Task> t2 = new ArrayList<>();
            t2.add(new Task(3, 1, "Hehe", "Mô tả", 0, "WORKING", "LOW", fmt.parse("25/04/2025"), true, 0, new Date(), null, 0, 2));
            phaseList.add(new Phase(2, 1, "Ds2", "", 1, new Date(), t2));

            List<Task> t3 = new ArrayList<>();
            t3.add(new Task(4, 1, "Hello 1", "Mô tả", 3, "WORKING", "LOW", fmt.parse("25/04/2025"), true, 0, new Date(), null, 3, 0));
            phaseList.add(new Phase(3, 1, "Ds3", "", 2, new Date(), t3));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void setupRecycler() {
        RecyclerView rv = findViewById(R.id.rvPhase);
        rv.setLayoutManager(
                new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        );
        phaseAdapter = new PhaseAdapter(
                phaseList,
                this,    // OnAddPhaseListener
                this,    // OnTaskActionListener
                this     // OnTaskDropListener
        );
        rv.setAdapter(phaseAdapter);
    }

    private void setupToolbar() {
        btnConfirm.setEnabled(false);
        btnConfirm.setAlpha(0.3f);

        btnCancel.setOnClickListener(v -> {
            phaseAdapter.cancelEditing();
            exitInputMode();
        });
        btnConfirm.setOnClickListener(v -> {
            if (editingPhase >= 0) {
                String name = phaseAdapter.getNewTaskName();
                if (!name.isEmpty()) {
                    onTaskAddConfirmed(editingPhase, name);
                }
            }
            phaseAdapter.cancelEditing();
            exitInputMode();
        });
    }

    @Override
    public void onAddPhase() {
        // Khi người dùng bấm "Thêm Phase"
        List<Task> newTasks = new ArrayList<>();
        newTasks.add(new Task(
                0, // sẽ cấp ID sau
                phaseList.size()+1,
                "Task mới",
                "",
                0, "WORKING", "LOW",
                null, true,
                0, new Date(), null,
                0, 0
        ));
        phaseList.add(new Phase(
                phaseList.size()+1,
                1,
                "Ds" + (phaseList.size()+1),
                "",
                phaseList.size(),
                new Date(),
                newTasks
        ));
        phaseAdapter.notifyItemInserted(phaseList.size()-1);
    }

    @Override
    public void onTaskAddRequested(int phasePosition) {
        editingPhase = phasePosition;
        phaseAdapter.startEditing(phasePosition);
        enterInputMode();
    }

    @Override
    public void onTaskAddConfirmed(int phasePosition, String taskName) {
        // Khi confirm nhập tên task
        Phase phase = phaseList.get(phasePosition);
        int newOrder = phase.getTask().size();
        phase.getTask().add(new Task(
                0, phase.getPhaseID(),
                taskName, "", 0,
                "WORKING", "LOW",
                null, true,
                newOrder, new Date(), null,
                0, 0
        ));
        phaseAdapter.notifyItemChanged(phasePosition);
    }

    @Override
    public void onTaskAddCanceled() {
        exitInputMode();
    }

    @Override
    public void onTaskNameChanged(int phasePosition, String newName) {
        if (phasePosition == editingPhase) {
            boolean ok = !newName.trim().isEmpty();
            btnConfirm.setEnabled(ok);
            btnConfirm.setAlpha(ok ? 1f : 0.3f);
        }
    }

    @Override
    public void onTaskDropped(Phase targetPhase, int dropIndex, DraggedTaskInfo info) {
        // Cập nhật lại UI sau khi kéo-thả task
        phaseAdapter.notifyDataSetChanged();
    }

    private void enterInputMode() {
        toolbarInput.setVisibility(View.VISIBLE);
        toolbarDetail.setVisibility(View.GONE);
    }

    private void exitInputMode() {
        editingPhase = -1;
        toolbarInput.setVisibility(View.GONE);
        toolbarDetail.setVisibility(View.VISIBLE);
    }
}
