package com.example.projectmanagement.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.DraggedTaskInfo;
import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.utils.PhaseTouchHelperCallback;

import java.util.Collections;
import java.util.List;

public class PhaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements PhaseTouchHelperCallback.ItemTouchHelperAdapter {

    RecyclerView rvBoard;
    private static final int TYPE_LIST = 0;
    private static final int TYPE_ADD = 1;
    private final List<Phase> phases;
    private final OnAddPhaseListener addPhaseListener;
    private final OnAddTaskListener addTaskListener;
    private final OnTaskActionListener taskListener;
    private final OnCardDropListener cardDropListener;

    // For edit mode
    private int editingPosition = -1;
    private String newTaskName = "";

    public interface OnAddPhaseListener {
        void onAddPhase();
    }
    public interface OnAddTaskListener {
        void onAddTask(int phasePosition);
    }
    public interface OnTaskActionListener {
        void onTaskAddRequested(int phasePosition);
        void onTaskAddConfirmed(int phasePosition, String taskName);
        void onTaskAddCanceled();
        void onTaskNameChanged(int phasePosition, String newName);
    }
    public interface OnCardDropListener {
        void onCardDropped(Phase targetList, int dropIndex, DraggedTaskInfo draggedInfo);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void startEditing(int pos) {
        editingPosition = pos;
        newTaskName = "";
        notifyDataSetChanged();
    }
    /** Hủy mode input */
    @SuppressLint("NotifyDataSetChanged")
    public void cancelEditing() {
        editingPosition = -1;
        newTaskName = "";
        notifyDataSetChanged();
    }
    /** Lấy text đang nhập */
    public String getNewTaskName() {
        return newTaskName.trim();
    }

    /**
     * Primary constructor with drag-and-drop support
     */
    public PhaseAdapter(
            List<Phase> phases,
            OnAddPhaseListener addPhaseListener,
            OnAddTaskListener addTaskListener,
            OnTaskActionListener taskListener,
            OnCardDropListener cardDropListener,
            RecyclerView board
    ) {
        this.phases = phases;
        this.addPhaseListener = addPhaseListener;
        this.addTaskListener = addTaskListener;
        this.taskListener = taskListener;
        this.cardDropListener = cardDropListener;
        this.rvBoard = board;
    }

    /**
     * Convenience constructor without drag-and-drop
     */

    @Override
    public int getItemViewType(int position) {
        if (position < phases.size()) {
            return TYPE_LIST;
        } else {
            return TYPE_ADD;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_LIST) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.phase_item, parent, false);
            return new PhaseViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.add_phase_item, parent, false);
            return new AddPhaseViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PhaseViewHolder) {
            PhaseViewHolder vh = (PhaseViewHolder) holder;
            int adapterPos = vh.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;

            Phase phase = phases.get(adapterPos);
            vh.tvPhaseTitle.setText(phase.getPhaseName());

            // Task list
            TaskAdapter taskAdapter = new TaskAdapter(phase.getTasks(), phase);
            vh.rvTask.setLayoutManager(new LinearLayoutManager(vh.itemView.getContext()));
            vh.rvTask.setAdapter(taskAdapter);


            PhaseTouchHelperCallback callback = new PhaseTouchHelperCallback(
                    taskAdapter::onItemMove,     // giả sử TaskAdapter có method onItemMove(int, int)
                    rvBoard                      // biến rvBoard bạn truyền từ Activity xuống Adapter
            );
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(vh.rvTask);

            // Edit mode
            if (adapterPos == editingPosition) {
                vh.etNew.setVisibility(View.VISIBLE);
                vh.etNew.setText(newTaskName);
                vh.tvAddTask.setVisibility(View.GONE);
                vh.etNew.requestFocus();

                InputMethodManager imm = (InputMethodManager)
                        vh.itemView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(vh.etNew, 0);

                vh.etNew.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                    @Override
                    public void onTextChanged(CharSequence s, int st, int b, int c) {
                        newTaskName = s.toString();
                        int pos = vh.getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION && taskListener != null) {
                            taskListener.onTaskNameChanged(pos, newTaskName);
                        }
                    }
                    @Override public void afterTextChanged(Editable e) {}
                });
            } else {
                vh.etNew.setVisibility(View.GONE);
                vh.tvAddTask.setVisibility(View.VISIBLE);
                vh.etNew.clearFocus();
            }

            vh.tvAddTask.setOnClickListener(v -> {
                int pos = vh.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && taskListener != null) {
                    taskListener.onTaskAddRequested(pos);
                }
            });

        } else if (holder instanceof AddPhaseViewHolder) {
            ((AddPhaseViewHolder) holder).tvAddPhase.setOnClickListener(v -> {
                if (addPhaseListener != null) addPhaseListener.onAddPhase();
            });
        }
    }
    @Override
    public int getItemCount() {
        // +1 cho nút "+ Thêm phase"
        return phases.size() + 1;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        // Chỉ hoán đổi giữa các item phase (không tính nút thêm)
        if (fromPosition < phases.size() && toPosition < phases.size()) {
            Collections.swap(phases, fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);
            return true;
        }
        return false;
    }
    class PhaseViewHolder extends RecyclerView.ViewHolder {
        final TextView tvPhaseTitle, tvAddTask;
        final RecyclerView rvTask;
        final EditText etNew;
        PhaseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPhaseTitle = itemView.findViewById(R.id.tvPhaseTitle);
            rvTask       = itemView.findViewById(R.id.rvTask);
            tvAddTask    = itemView.findViewById(R.id.tvAddTask);
            etNew        = itemView.findViewById(R.id.etNewTaskName);
        }
    }

    static class AddPhaseViewHolder extends RecyclerView.ViewHolder {
        final TextView tvAddPhase;
        AddPhaseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAddPhase = itemView.findViewById(R.id.tvAddPhase);
        }
    }
}
