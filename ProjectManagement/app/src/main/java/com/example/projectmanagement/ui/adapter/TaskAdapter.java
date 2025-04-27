package com.example.projectmanagement.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.DraggedTaskInfo;
import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.data.model.Task;
import com.example.projectmanagement.utils.ParseDateUtil;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_TASK = 0;
    private static final int TYPE_PLACEHOLDER = 1;

    private final Phase parentPhase;
    private final OnTaskDropListener dropListener;
    private final List<Task> tasks = new ArrayList<>();
    private int placeholderPosition = -1;

    public interface OnTaskDropListener {
        void onTaskDropped(Phase targetPhase, int dropIndex, DraggedTaskInfo info);
    }

    public TaskAdapter(Phase parentPhase, OnTaskDropListener listener) {
        setHasStableIds(true);
        this.parentPhase = parentPhase;
        this.dropListener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setTasks(List<Task> newTasks) {
        tasks.clear();
        if (newTasks != null) tasks.addAll(newTasks);
        notifyDataSetChanged();
    }

    public void setPlaceholder(int pos) {
        int old = placeholderPosition;
        if (pos < 0 || pos > tasks.size()) pos = -1;
        placeholderPosition = pos;
        if (old != -1) notifyItemRemoved(old);
        if (placeholderPosition != -1) notifyItemInserted(placeholderPosition);
    }

    public void clearPlaceholder() {
        if (placeholderPosition != -1) {
            int old = placeholderPosition;
            placeholderPosition = -1;
            notifyItemRemoved(old);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == placeholderPosition ? TYPE_PLACEHOLDER : TYPE_TASK;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType
    ) {
        if (viewType == TYPE_PLACEHOLDER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.task_placeholder, parent, false);
            return new PlaceholderViewHolder(v);
        }
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_TASK) {
            int actual = position;
            if (placeholderPosition != -1 && position > placeholderPosition) actual--;
            ((TaskViewHolder) holder).bind(tasks.get(actual));
        }
    }

    @Override public long getItemId(int position) {
        if (getItemViewType(position) == TYPE_PLACEHOLDER) return -1;
        int actual = position;
        if (placeholderPosition != -1 && position > placeholderPosition) actual--;
        return tasks.get(actual).getTaskID();
    }

    @Override public int getItemCount() {
        return tasks.size() + (placeholderPosition != -1 ? 1 : 0);
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        CheckBox checkBox;
        TextView tvTitle, tvDate, tvAvatar, tvCmt, tvFile, tvDesc;
        LinearLayout rowDate, rowInfo, rowAvatar;
        ImageView imgCmt, imgFile;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            card     = itemView.findViewById(R.id.cardTask);
            checkBox = itemView.findViewById(R.id.checkBoxTask);
            tvTitle  = itemView.findViewById(R.id.tvTaskTitle);
            tvDesc   = itemView.findViewById(R.id.tvTaskDes);
            tvDate   = itemView.findViewById(R.id.tvDateRange);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvCmt    = itemView.findViewById(R.id.tvCommentCount);
            tvFile   = itemView.findViewById(R.id.tvFileCnt);
            rowDate  = itemView.findViewById(R.id.layoutInfoRow);
            rowInfo  = itemView.findViewById(R.id.layoutInfoRow1);
            rowAvatar= itemView.findViewById(R.id.layoutInfoRow2);
            imgCmt   = itemView.findViewById(R.id.imgComment);
            imgFile  = itemView.findViewById(R.id.imgfile);

            itemView.setOnLongClickListener(v -> {
                int adapterPos = getAdapterPosition();
                if (adapterPos == RecyclerView.NO_POSITION) return false;
                int actual = adapterPos;
                if (placeholderPosition != -1 && adapterPos > placeholderPosition) actual--;
                Task t = tasks.remove(actual);
                notifyItemRemoved(adapterPos);
                DraggedTaskInfo info = new DraggedTaskInfo(
                        t, parentPhase, actual, v.getWidth(), v.getHeight());
                v.startDrag(null, new View.DragShadowBuilder(v), info, 0);
                return true;
            });
        }

        void bind(Task task) {
            tvTitle.setText(task.getTaskName());

            // checkbox + stroke
            checkBox.setOnCheckedChangeListener(null);
            boolean done = "DONE".equals(task.getStatus());
            checkBox.setChecked(done);
            int color = itemView.getContext()
                    .getColor(done ? R.color.card_stroke_checked : R.color.card_stroke_default);
            card.setStrokeColor(color);
            checkBox.setOnCheckedChangeListener((cb, checked) -> {
                card.setStrokeColor(
                        itemView.getContext().getColor(
                                checked ? R.color.card_stroke_checked : R.color.card_stroke_default));
                task.setStatus(checked ? "DONE" : "WORKING");
            });

            // due date
            if (task.getDueDate() != null) {
                rowDate.setVisibility(View.VISIBLE);
                tvDate.setText(ParseDateUtil.formatDate(task.getDueDate()));
            } else rowDate.setVisibility(View.GONE);

            // comments & files
            boolean hasCmt  = task.getCmtCnt()  != null && task.getCmtCnt() > 0;
            boolean hasFile = task.getFileCnt() != null && task.getFileCnt()> 0;
            if (!hasCmt && !hasFile) {
                rowInfo.setVisibility(View.GONE);
            } else {
                rowInfo.setVisibility(View.VISIBLE);
                imgCmt.setVisibility(hasCmt ? View.VISIBLE:View.GONE);
                tvCmt.setText(hasCmt? String.valueOf(task.getCmtCnt()):"");
                imgFile.setVisibility(hasFile? View.VISIBLE:View.GONE);
                tvFile.setText(hasFile? String.valueOf(task.getFileCnt()):"");
            }

            // avatar
            if (task.getAssignedTo() > 0) {
                rowAvatar.setVisibility(View.VISIBLE);
                tvAvatar.setText(String.valueOf(task.getAssignedTo()));
            } else rowAvatar.setVisibility(View.GONE);

            // description
            if (task.getTaskDescription() != null && !task.getTaskDescription().isEmpty()) {
                tvDesc.setVisibility(View.VISIBLE);
                tvDesc.setText(task.getTaskDescription());
            } else tvDesc.setVisibility(View.GONE);
        }
    }
    static class PlaceholderViewHolder extends RecyclerView.ViewHolder {
        PlaceholderViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
