package com.example.projectmanagement.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.Task;
import com.example.projectmanagement.utils.ParseDateUtil;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks;

    public TaskAdapter(List<Task> tasks) {
        this.tasks = tasks != null ? tasks : new ArrayList<>();
    }

    /** Gọi từ ViewHolder của PhaseAdapter để update list */
    public void setTasks(List<Task> tasks) {
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.tvTaskTitle.setText(task.getTaskName());

        // tránh listener cũ
        holder.checkBoxTask.setOnCheckedChangeListener(null);
        // set state checkbox
        boolean done = "DONE".equals(task.getStatus());
        holder.checkBoxTask.setChecked(done);
        // set màu viền ban đầu
        updateCardStroke(holder, done);

        // gắn listener mới
        holder.checkBoxTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // đổi màu viền
            updateCardStroke(holder, isChecked);
            // nếu muốn cập nhật model:
            task.setStatus(isChecked ? "DONE" : "WORKING");
        });

        bindDate(holder, task);
        bindCommentAndFile(holder, task);
        bindAvatar(holder, task);
        binDescription(holder, task);
    }

    private void updateCardStroke(TaskViewHolder holder, boolean isChecked) {
        int color = isChecked
                ? ContextCompat.getColor(holder.itemView.getContext(), R.color.card_stroke_checked)
                : ContextCompat.getColor(holder.itemView.getContext(), R.color.card_stroke_default);
        holder.cardTask.setStrokeColor(color);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView cardTask;
        CheckBox checkBoxTask;
        TextView tvTaskTitle, tvDateRange, tvAvatar, tvCommentCount, tvFileCnt, tvTaskDes;
        LinearLayout layoutInfoRow, layoutInfoRow1, layoutInfoRow2;
        ImageView imgComment, imgfile;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardTask      = itemView.findViewById(R.id.cardTask);
            checkBoxTask    = itemView.findViewById(R.id.checkBoxTask);
            tvTaskTitle    = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskDes = itemView.findViewById(R.id.tvTaskDes);
            tvDateRange    = itemView.findViewById(R.id.tvDateRange);
            tvAvatar       = itemView.findViewById(R.id.tvAvatar);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
            tvFileCnt      = itemView.findViewById(R.id.tvFileCnt);

            layoutInfoRow  = itemView.findViewById(R.id.layoutInfoRow);
            layoutInfoRow1 = itemView.findViewById(R.id.layoutInfoRow1);
            layoutInfoRow2 = itemView.findViewById(R.id.layoutInfoRow2);

            imgComment = itemView.findViewById(R.id.imgComment);
            imgfile    = itemView.findViewById(R.id.imgfile);

        }
    }

    private void bindDate(TaskViewHolder holder, Task task) {
        if (task.getDueDate() != null) {
            holder.layoutInfoRow.setVisibility(View.VISIBLE);
            holder.tvDateRange.setText(ParseDateUtil.formatDate(task.getDueDate()));
        } else {
            holder.layoutInfoRow.setVisibility(View.GONE);
        }
    }

    private void bindCommentAndFile(TaskViewHolder holder, Task task) {
        boolean hasCmt  = task.getCmtCnt() != null && task.getCmtCnt() > 0;
        boolean hasFile = task.getFileCnt() != null && task.getFileCnt() > 0;

        if (!hasCmt && !hasFile) {
            holder.layoutInfoRow1.setVisibility(View.GONE);
            return;
        }
        holder.layoutInfoRow1.setVisibility(View.VISIBLE);

        if (hasCmt) {
            holder.imgComment.setVisibility(View.VISIBLE);
            holder.tvCommentCount.setText(String.valueOf(task.getCmtCnt()));
        } else {
            holder.imgComment.setVisibility(View.GONE);
        }

        if (hasFile) {
            holder.imgfile.setVisibility(View.VISIBLE);
            holder.tvFileCnt.setText(String.valueOf(task.getFileCnt()));
        } else {
            holder.imgfile.setVisibility(View.GONE);
        }
    }

    private void bindAvatar(TaskViewHolder holder, Task task) {
        if (task.getAssignedTo() != 0 ) {
            holder.layoutInfoRow2.setVisibility(View.VISIBLE);
            holder.tvAvatar.setText(String.valueOf(task.getAssignedTo()));
        } else {
            holder.layoutInfoRow2.setVisibility(View.GONE);
        }
    }

    private void binDescription(TaskViewHolder holder, Task task){
        if(!task.getTaskDescription().isEmpty()){
            holder.tvTaskDes.setVisibility(View.VISIBLE);
            holder.tvTaskDes.setText(String.valueOf(task.getTaskDescription()));
        }else{
            holder.tvTaskDes.setVisibility(View.GONE);
        }
    }
}
