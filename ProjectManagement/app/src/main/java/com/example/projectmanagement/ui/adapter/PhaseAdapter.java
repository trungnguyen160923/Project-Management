package com.example.projectmanagement.ui.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.DraggedTaskInfo;
import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.data.model.Task;

import java.util.Collections;
import java.util.List;

/**
 * Adapter cho danh sách các Phase (cột) và bên trong mỗi cột là RecyclerView các Task,
 * hỗ trợ:
 *  - Thêm Phase mới
 *  - Thêm Task mới (vừa edit vừa xác nhận/hủy)
 *  - Kéo & thả Task giữa các Phase
 */
public class PhaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_PHASE = 0, TYPE_ADD = 1;

    private final List<Phase> phases;
    private final OnAddPhaseListener addPhaseListener;
    private final OnTaskActionListener taskListener;
    private final TaskAdapter.OnTaskDropListener dropListener;

    // trạng thái edit tên Task
    private int editingPosition = RecyclerView.NO_POSITION;
    private String newTaskName = "";

    public interface OnAddPhaseListener {
        void onAddPhase();
    }

    public interface OnTaskActionListener {
        void onTaskAddRequested(int phasePosition);
        void onTaskAddConfirmed(int phasePosition, String taskName);
        void onTaskAddCanceled();
        void onTaskNameChanged(int phasePosition, String newName);
    }

    public PhaseAdapter(
            List<Phase> phases,
            OnAddPhaseListener addPhaseListener,
            OnTaskActionListener taskListener,
            TaskAdapter.OnTaskDropListener dropListener
    ) {
        this.phases = phases;
        this.addPhaseListener = addPhaseListener;
        this.taskListener = taskListener;
        this.dropListener = dropListener;
    }

    @Override
    public int getItemViewType(int position) {
        return position < phases.size() ? TYPE_PHASE : TYPE_ADD;
    }

    @Override
    public int getItemCount() {
        return phases.size() + 1;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType
    ) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_PHASE) {
            View v = inf.inflate(R.layout.phase_item, parent, false);
            return new PhaseViewHolder(v);
        } else {
            View v = inf.inflate(R.layout.add_phase_item, parent, false);
            return new AddPhaseViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder, int position
    ) {
        if (holder instanceof PhaseViewHolder) {
            ((PhaseViewHolder) holder).bind(phases.get(position), position);
        } else {
            ((AddPhaseViewHolder) holder).bind();
        }
    }


    /** Bắt đầu edit thêm Task ở cột position */
    public void startEditing(int pos) {
        int prev = editingPosition;
        editingPosition = pos;
        newTaskName = "";
        if (prev != RecyclerView.NO_POSITION) notifyItemChanged(prev);
        notifyItemChanged(pos);
    }

    /** Hủy edit */
    public void cancelEditing() {
        int prev = editingPosition;
        editingPosition = RecyclerView.NO_POSITION;
        newTaskName = "";
        if (prev != RecyclerView.NO_POSITION) {
            notifyItemChanged(prev);
        }
        taskListener.onTaskAddCanceled();
    }

    /** Lấy tên Task đang nhập */
    public String getNewTaskName() {
        return newTaskName.trim();
    }

    /** ViewHolder cho mỗi Phase (cột) */
    class PhaseViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAddTask;
        EditText etNew;
        RecyclerView rvTasks;
        TaskAdapter taskAdapter;
        TextWatcher watcher;

        PhaseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle   = itemView.findViewById(R.id.tvPhaseTitle);
            tvAddTask = itemView.findViewById(R.id.tvAddTask);
            etNew     = itemView.findViewById(R.id.etNewTaskName);
            rvTasks   = itemView.findViewById(R.id.rvTask);

            // set listener cho nút "Thêm Task"
            tvAddTask.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    startEditing(pos);
                    taskListener.onTaskAddRequested(pos);
                }
            });

            // bắt sự kiện nhấn Done trên bàn phím
            etNew.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        taskListener.onTaskAddConfirmed(pos, getNewTaskName());
                        cancelEditing();
                    }
                    return true;
                }
                return false;
            });
        }

        void bind(Phase phase, int position) {
            // Tiêu đề cột
            tvTitle.setText(phase.getPhaseName());

            // Setup TaskAdapter cho RecyclerView con
            // Sort tasks theo orderIndex trước khi hiển thị
            Collections.sort(phase.getTask(), (a,b) ->
                    Integer.compare(a.getOrderIndex(), b.getOrderIndex())
            );

            taskAdapter = new TaskAdapter(phase, dropListener);
            taskAdapter.setTasks(phase.getTask());

            rvTasks.setLayoutManager(
                    new LinearLayoutManager(itemView.getContext())
            );
            rvTasks.setAdapter(taskAdapter);

            // Bật drag listener trên mỗi Task RecyclerView
            rvTasks.setOnDragListener((v, event) -> {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        // chỉ chấp nhận nếu localState là DraggedTaskInfo
                        return event.getLocalState() instanceof DraggedTaskInfo;
                    case DragEvent.ACTION_DRAG_LOCATION:
                        float y = event.getY();
                        int idx = calculateInsertionIndex(y);
                        taskAdapter.setPlaceholder(idx);
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:
                    case DragEvent.ACTION_DRAG_ENDED:
                        taskAdapter.clearPlaceholder();
                        break;
                    case DragEvent.ACTION_DROP:
                        float dropY = event.getY();
                        int dropIdx = Math.max(0, Math.min(
                                calculateInsertionIndex(dropY),
                                phase.getTask().size()
                        ));
                        taskAdapter.clearPlaceholder();

                        DraggedTaskInfo info =
                                (DraggedTaskInfo) event.getLocalState();

                        // thêm vào đúng phase và vị trí
                        phase.getTask().add(dropIdx, info.task);
                        // cập nhật orderIndex
                        for (int i = 0; i < phase.getTask().size(); i++) {
                            phase.getTask().get(i).setOrderIndex(i);
                        }
                        dropListener.onTaskDropped(phase, dropIdx, info);
                        break;
                }
                return true;
            });

            // Hiển thị hoặc ẩn edit field
            boolean editing = position == editingPosition;
            etNew.setVisibility(editing ? View.VISIBLE : View.GONE);
            tvAddTask.setVisibility(editing ? View.GONE : View.VISIBLE);

            if (editing) {
                etNew.setText(newTaskName);
                etNew.requestFocus();
                if (watcher != null) etNew.removeTextChangedListener(watcher);
                watcher = new TextWatcher() {
                    @Override public void beforeTextChanged(
                            CharSequence s, int st, int b, int c
                    ) {}
                    @Override public void onTextChanged(
                            CharSequence s, int st, int b, int c
                    ) {
                        newTaskName = s.toString();
                        taskListener.onTaskNameChanged(position, newTaskName);
                    }
                    @Override public void afterTextChanged(Editable s) {}
                };
                etNew.addTextChangedListener(watcher);
            } else {
                if (watcher != null) {
                    etNew.removeTextChangedListener(watcher);
                    watcher = null;
                }
                etNew.clearFocus();
                etNew.setText("");
            }
        }

        private int calculateInsertionIndex(float y) {
            int count = rvTasks.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = rvTasks.getChildAt(i);
                if (y < child.getTop() + child.getHeight()/2f) {
                    return i;
                }
            }
            return taskAdapter.getItemCount();
        }
    }

    /** ViewHolder cho phần "Thêm Phase mới" ở cuối */
    private class AddPhaseViewHolder extends RecyclerView.ViewHolder {
        TextView tvAddPhase;
        AddPhaseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAddPhase = itemView.findViewById(R.id.tvAddPhase);
        }
        void bind() {
            tvAddPhase.setOnClickListener(v -> addPhaseListener.onAddPhase());
        }
    }
}
