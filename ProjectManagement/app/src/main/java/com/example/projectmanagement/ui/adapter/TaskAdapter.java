package com.example.projectmanagement.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.DraggedTaskInfo;
import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.data.model.Task;
import com.example.projectmanagement.utils.ParseDateUtil;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int TYPE_TASK = 1;
    private static final int TYPE_PLACEHOLDER = 2;

    private  List<Task> tasks;
    private  Phase parentPhase;
    private int placeholderPosition = -1;

    public TaskAdapter(List<Task> tasks, Phase parentPhase) {
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        this.parentPhase = parentPhase;
    }

    /** Gọi từ ViewHolder của PhaseAdapter để update list */
    public void setTasks(List<Task> tasks) {
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setPlaceholderPosition(int position) {
        if (position < 0) position = 0;
        if (position > tasks.size()) position = tasks.size();
        if (position != placeholderPosition) {
            placeholderPosition = position;
            notifyDataSetChanged();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearPlaceholder() {
        placeholderPosition = -1;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (placeholderPosition != -1 && position == placeholderPosition) {
            return TYPE_PLACEHOLDER;
        }
        return TYPE_TASK;
    }

    @Override
    public int getItemCount() {
        return tasks.size() + (placeholderPosition != -1 ? 1 : 0);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_TASK) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
            return new TaskViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_placeholder, parent, false);
            return new PlaceholderViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_TASK && holder instanceof TaskViewHolder) {
            int actualPosition = position;
            if (placeholderPosition != -1 && position > placeholderPosition) {
                actualPosition = position - 1;
            }
            Task task = tasks.get(actualPosition);
            ((TaskViewHolder) holder).tvTaskTitle.setText(task.getTaskName());

            // tránh listener cũ
            ((TaskViewHolder) holder).checkBoxTask.setOnCheckedChangeListener(null);
            // set state checkbox
            boolean done = "DONE".equals(task.getStatus());
            ((TaskViewHolder) holder).checkBoxTask.setChecked(done);
            // set màu viền ban đầu
            updateCardStroke(((TaskViewHolder) holder), done);

            // gắn listener mới
            ((TaskViewHolder) holder).checkBoxTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // đổi màu viền
                updateCardStroke(((TaskViewHolder) holder), isChecked);
                // nếu muốn cập nhật model:
                task.setStatus(isChecked ? "DONE" : "WORKING");
            });

            bindDate(((TaskViewHolder) holder), task);
            bindCommentAndFile(((TaskViewHolder) holder), task);
            bindAvatar(((TaskViewHolder) holder), task);
            binDescription(((TaskViewHolder) holder), task);
        }
    }


    private void updateCardStroke(TaskViewHolder holder, boolean isChecked) {
        int color = isChecked
                ? ContextCompat.getColor(holder.itemView.getContext(), R.color.card_stroke_checked)
                : ContextCompat.getColor(holder.itemView.getContext(), R.color.card_stroke_default);
        holder.cardTask.setStrokeColor(color);
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

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

            itemView.setOnClickListener(v ->
                    Toast.makeText(v.getContext(), tvTaskTitle.getText(), Toast.LENGTH_SHORT).show());
            itemView.setOnLongClickListener(v -> {
                int adapterPosition = getAdapterPosition();
                int actualPosition = adapterPosition;
                if (placeholderPosition != -1 && adapterPosition > placeholderPosition) {
                    actualPosition = adapterPosition - 1;
                }
                Task Task = tasks.get(actualPosition);
                int TaskWidth = v.getWidth();
                int TaskHeight = v.getHeight();
                DraggedTaskInfo info = new DraggedTaskInfo(Task, parentPhase, actualPosition, TaskWidth, TaskHeight);
                v.startDrag(null, new View.DragShadowBuilder(v), info, 0);
                tasks.remove(actualPosition);
                notifyItemRemoved(adapterPosition);
                return true;
            });

        }
    }

    class PlaceholderViewHolder extends RecyclerView.ViewHolder {
        public PlaceholderViewHolder(@NonNull View itemView) {
            super(itemView);
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
