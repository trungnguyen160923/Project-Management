package com.example.projectmanagement.ui.adapter;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.data.model.Task;

import java.util.List;

public class PhaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int TYPE_LIST = 0;
    private static final int TYPE_ADD = 1;

    private List<Phase> phaseList;

    private OnAddPhaseListener addPhaseListener;
    private OnAddTaskListener addTaskListener;
    private final OnTaskActionListener taskListener;


    public interface OnAddPhaseListener {
        void onAddPhase();
    }

    public interface OnAddTaskListener {
        void onAddTask(int phasePosition);
    }

    public interface OnTaskActionListener {
        /** Gọi khi user bấm "+ Thêm thẻ" */
        void onTaskAddRequested(int phasePosition);
        /** Gọi khi user xác nhận (✓) trên Toolbar */
        void onTaskAddConfirmed(int phasePosition, String taskName);
        /** Gọi khi user hủy (X) trên Toolbar */
        void onTaskAddCanceled();

        void onTaskNameChanged(int phasePosition, String newName);
    }

    // Vị trí đang edit, -1 nếu không có
    private int editingPosition = -1;
    // Giá trị text hiện tại
    private String newTaskName = "";


    public void startEditing(int pos) {
        editingPosition = pos;
        newTaskName = "";
        notifyDataSetChanged();
    }
    /** Hủy mode input */
    public void cancelEditing() {
        editingPosition = -1;
        newTaskName = "";
        notifyDataSetChanged();
    }
    /** Lấy text đang nhập */
    public String getNewTaskName() {
        return newTaskName.trim();
    }

    public PhaseAdapter(List<Phase> phaseList, OnAddPhaseListener addPhaseListener, OnAddTaskListener addTaskListener, OnTaskActionListener taskListener) {
        this.phaseList = phaseList;
        this.addPhaseListener = addPhaseListener;
        this.addTaskListener = addTaskListener;
        this.taskListener = taskListener;
    }


    @Override
    public int getItemViewType(int position) {
        if (position < phaseList.size()) {
            return TYPE_LIST;
        } else {
            return TYPE_ADD;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_LIST) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.phase_item, parent, false);
            return new PhaseViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_phase_item, parent, false);
            return new AddPhaseViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PhaseViewHolder) {
            Phase phase = phaseList.get(position);
            ((PhaseViewHolder) holder).tvPhaseTitle.setText(phase.getPhaseName());
            // Khởi tạo adapter cho danh sách task trong ListCard
            // Giả sử bạn có một adapter TaskAdapter để hiển thị các task trong listCard.getCards()
            TaskAdapter taskAdapter = new TaskAdapter((List<Task>) phase.getTask());
            ((PhaseViewHolder) holder).rvTask.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
            ((PhaseViewHolder) holder).rvTask.setAdapter(taskAdapter);

            if(position == editingPosition){
                ((PhaseViewHolder) holder).etNew.setVisibility(View.VISIBLE);
                ((PhaseViewHolder) holder).etNew.setText(newTaskName);
                ((PhaseViewHolder) holder).tvAddTask.setVisibility(View.GONE);
                ((PhaseViewHolder) holder).etNew.requestFocus();

                // bật keyboard
                InputMethodManager imm = (InputMethodManager)
                        holder.itemView.getContext()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(((PhaseViewHolder) holder).etNew, 0);
                // TextWatcher để cập nhật newTaskName
                ((PhaseViewHolder) holder).etNew.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                    @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                        newTaskName = s.toString();
                        if (taskListener != null) {
                            taskListener.onTaskNameChanged(position, newTaskName);
                        }
                    }

                    @Override public void afterTextChanged(Editable e) {}
                });
            } else {
            // chế độ bình thường
            ((PhaseViewHolder) holder).etNew.setVisibility(View.GONE);
            ((PhaseViewHolder) holder).tvAddTask.setVisibility(View.VISIBLE);
            ((PhaseViewHolder) holder).etNew.clearFocus();
            }

            // khi click "+ Thêm thẻ"
            ((PhaseViewHolder) holder).tvAddTask.setOnClickListener(v -> {
                if (taskListener  != null) {
                    taskListener .onTaskAddRequested(position);
                }
            });


        } else if (holder instanceof AddPhaseViewHolder) {
            ((AddPhaseViewHolder) holder).tvAddPhase.setOnClickListener(v -> {
                if (addPhaseListener != null) {
                    addPhaseListener.onAddPhase();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return phaseList.size() + 1;
    }

    static class PhaseViewHolder extends RecyclerView.ViewHolder {
        TextView tvPhaseTitle, tvAddTask;
        RecyclerView rvTask;
        EditText etNew;
        public PhaseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPhaseTitle = itemView.findViewById(R.id.tvPhaseTitle);
            rvTask = itemView.findViewById(R.id.rvTask);
            tvAddTask = itemView.findViewById(R.id.tvAddTask);
            etNew   = itemView.findViewById(R.id.etNewTaskName);
        }
    }

    static class AddPhaseViewHolder extends RecyclerView.ViewHolder {
        TextView tvAddPhase;
        public AddPhaseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAddPhase = itemView.findViewById(R.id.tvAddPhase);
        }
    }
}
