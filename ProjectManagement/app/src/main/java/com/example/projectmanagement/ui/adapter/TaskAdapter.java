package com.example.projectmanagement.ui.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.convertor.FileConvertor;
import com.example.projectmanagement.data.model.DraggedTaskInfo;
import com.example.projectmanagement.data.model.File;
import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.data.model.ProjectHolder;
import com.example.projectmanagement.data.model.ProjectMember;
import com.example.projectmanagement.data.model.ProjectMemberHolder;
import com.example.projectmanagement.data.model.Task;
import com.example.projectmanagement.data.service.FileService;
import com.example.projectmanagement.data.service.TaskService;
import com.example.projectmanagement.ui.task.TaskActivity;
import com.example.projectmanagement.utils.Helpers;
import com.example.projectmanagement.utils.ParseDateUtil;
import com.google.android.material.card.MaterialCardView;
import com.example.projectmanagement.utils.UserPreferences;
import com.example.projectmanagement.data.model.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "TaskAdapter";

    private static final int TYPE_TASK = 1;
    private static final int TYPE_PLACEHOLDER = 2;

    private List<Task> tasks;
    private Phase parentPhase;
    private int placeholderPosition = -1;
    private int placeholderWidth = ViewGroup.LayoutParams.MATCH_PARENT;
    private int placeholderHeight = ViewGroup.LayoutParams.WRAP_CONTENT;

    private TaskService taskService;

    public TaskAdapter(List<Task> tasks, Phase parentPhase) {
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        this.parentPhase = parentPhase;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setPlaceholderPosition(int pos, int width, int height) {
        if (pos < 0 || pos > tasks.size()) {
            clearPlaceholder();
            return;
        }
        this.placeholderPosition = pos;
        this.placeholderWidth = width;
        this.placeholderHeight = height;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setTasks(List<Task> tasks) {
        Log.d("TaskAdapter", "Setting tasks: " + (tasks != null ? tasks.size() : 0) + " tasks");
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        clearPlaceholder();
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setPlaceholderPosition(int position) {
        int old = placeholderPosition;
        if (old != -1) notifyItemRemoved(old);
        placeholderPosition = position;
        if (position != -1) notifyItemInserted(position);
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

    @Override
    public int getItemCount() {
        return tasks.size() + (placeholderPosition != -1 ? 1 : 0);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_TASK) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.task_item, parent, false);
            return new TaskViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.task_placeholder, parent, false);
            return new PlaceholderViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_TASK) {
            int actualPos = position;
            if (placeholderPosition != -1 && position > placeholderPosition) {
                actualPos = position - 1;
            }
            TaskViewHolder vh = (TaskViewHolder) holder;
            Task task = tasks.get(actualPos);
            
            // Set background color based on task status
            MaterialCardView cardView = holder.itemView.findViewById(R.id.cardTask);
            CheckBox checkBoxTask = holder.itemView.findViewById(R.id.checkBoxTask);
            Log.d("TESTCARDCOLOR", "CardView found: " + (cardView != null));
            
            UserPreferences userPreferences = new UserPreferences(holder.itemView.getContext());
            User currentUser = userPreferences.getUser();
            Log.d("TESTCARDCOLOR", "Current user ID: " + (currentUser != null ? currentUser.getId() : "null"));
            Log.d("TESTCARDCOLOR", "Task assigned to: " + task.getAssignedTo());
            
            // Hide checkbox if task is not assigned to current user
            if (task.getAssignedTo() != (currentUser != null ? currentUser.getId() : -1)) {
                checkBoxTask.setVisibility(View.GONE);
            } else {
                checkBoxTask.setVisibility(View.VISIBLE);
            }
            
            // Check if task is overdue
            boolean isOverdue = false;
            if (task.getDueDate() != null) {
                isOverdue = task.getDueDate().before(new Date()) && !task.getStatus().equals("DONE");
            }
            
            // Set background color and border based on task status
            if (task.getStatus().equals("DONE")) {
                // Task is completed - light green background
                int color = ContextCompat.getColor(holder.itemView.getContext(), R.color.completed_green);
                cardView.setCardBackgroundColor(Color.argb(60, Color.red(color), Color.green(color), Color.blue(color)));
                cardView.setStrokeColor(color);
                cardView.setStrokeWidth(1);
                Log.d("TESTCARDCOLOR", "Task completed - setting light green background");
            } else if (isOverdue) {
                // Task is overdue - red border
                int color = ContextCompat.getColor(holder.itemView.getContext(), R.color.red);
                cardView.setCardBackgroundColor(Color.argb(80, Color.red(color), Color.green(color), Color.blue(color)));
                cardView.setStrokeColor(color);
                cardView.setStrokeWidth(1);
                Log.d("TESTCARDCOLOR", "Task overdue - setting red border");
            } else if (task.getAssignedTo() == (currentUser != null ? currentUser.getId() : -1)) {
                // Task is assigned to current user - light yellow background
                int color = ContextCompat.getColor(holder.itemView.getContext(), R.color.yellow_light);
                cardView.setCardBackgroundColor(Color.argb(60, Color.red(color), Color.green(color), Color.blue(color)));
                cardView.setStrokeColor(color);
                cardView.setStrokeWidth(1);
                Log.d("TESTCARDCOLOR", "Task assigned to current user - setting light yellow background");
            } else {
                // Default white background
                cardView.setCardBackgroundColor(Color.WHITE);
                cardView.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.card_stroke_default));
                cardView.setStrokeWidth(2);
                Log.d("TESTCARDCOLOR", "Setting default white background");
            }
            
            cardView.invalidate();
            holder.itemView.invalidate();
            
            vh.bind(task, parentPhase, tasks);
        } else {
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            lp.width = placeholderWidth;
            lp.height = placeholderHeight;
            holder.itemView.setLayoutParams(lp);
        }
    }

    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(tasks, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(tasks, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardTask;
        CheckBox checkBoxTask;
        TextView tvTaskTitle, tvDateRange, tvAvatar, tvCommentCount, tvFileCnt, tvTaskDes;
        LinearLayout layoutInfoRow, layoutInfoRow1, layoutInfoRow2;
        ImageView imgComment, imgfile;
        Context context;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            cardTask = itemView.findViewById(R.id.cardTask);
            checkBoxTask = itemView.findViewById(R.id.checkBoxTask);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskDes = itemView.findViewById(R.id.tvTaskDes);
            tvDateRange = itemView.findViewById(R.id.tvDateRange);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
            tvFileCnt = itemView.findViewById(R.id.tvFileCnt);
            layoutInfoRow = itemView.findViewById(R.id.layoutInfoRow);
            layoutInfoRow1 = itemView.findViewById(R.id.layoutInfoRow1);
            layoutInfoRow2 = itemView.findViewById(R.id.layoutInfoRow2);
            imgComment = itemView.findViewById(R.id.imgComment);
            imgfile = itemView.findViewById(R.id.imgfile);
        }

        void bind(Task t, Phase parentPhase, List<Task> tasks) {
            // Populate UI
            tvTaskTitle.setText(t.getTaskName());
            bindDate(this, t);
            bindCommentAndFile(this, t);
            bindAvatar(this, t);
            binDescription(this, t);

            // Checkbox state
            checkBoxTask.setOnCheckedChangeListener(null);
            boolean done = "DONE".equals(t.getStatus());
            checkBoxTask.setChecked(done);
            updateCardStroke(this, done);
            checkBoxTask.setOnCheckedChangeListener((btn, isChecked) -> {
                Log.d(TAG, ">>> run this");
                updateCardStroke(this, isChecked);
                String status = isChecked ? "DONE" : "IN_PROGRESS";
                taskService.markTaskAsComplette(itemView.getContext(), t.getTaskID(), status, res -> {
                    // Update task status in ProjectHolder after successful API call
                    Project currentProject = ProjectHolder.get();
                    if (currentProject != null && currentProject.getPhases() != null) {
                        for (Phase phase : currentProject.getPhases()) {
                            if (phase.getTasks() != null) {
                                for (Task task : phase.getTasks()) {
                                    if (task.getTaskID() == t.getTaskID()) {
                                        task.setStatus(status);
                                        break;
                                    }
                                }
                            }
                        }
                        ProjectHolder.set(currentProject);
                    }
                }, err -> {
                    String errorMessage = "Lỗi không xác định";
                    try {
                        errorMessage = Helpers.parseError(err);
                    } catch (Exception e) {
                    }
                    Toast.makeText(itemView.getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                });
                t.setStatus(isChecked ? "DONE" : "IN_PROGRESS");
            });

            // Click -> TaskActivity
            itemView.setOnClickListener(v -> {
                Context ctx = v.getContext();
                Intent intent = new Intent(ctx, TaskActivity.class);
                Bundle bundle = new Bundle();
                FileService.fetchFilesByTask(context, t.getTaskID(), res -> {
                    JSONArray data = res.optJSONArray("data");
                    List<File> files = new ArrayList<>();
                    if (data != null) {
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject jsonObject = data.optJSONObject(i);
                            Log.d(TAG, ">>> fetched task at task adapter: " + jsonObject);
                            File file = FileConvertor.fromJson(jsonObject);
                            files.add(file);
                        }
                    }
                    Log.d(TAG, ">>> files at task adapter bind click: " + files);
                    t.setFiles(files);
                    bundle.putParcelable("task", t);
                    intent.putExtras(bundle);
                    if (ctx instanceof Activity) {
                        Log.d(TAG, "Starting TaskActivity with requestCode 1001");
                        ((Activity) ctx).startActivityForResult(intent, 1001);
                    } else {
                        Log.d(TAG, "Starting TaskActivity without requestCode");
                        ctx.startActivity(intent);
                    }
                }, err -> {
                    String errMsg = "Không thể lấy danhs sách các file";
                    try {
                        errMsg = Helpers.parseError(err);
                    } catch (Exception e) {
                    }
                    Toast.makeText(ctx, errMsg, Toast.LENGTH_SHORT).show();
                });
            });

            // Thêm click listener riêng cho checkbox
            checkBoxTask.setOnClickListener(v -> {
                // Ngăn sự kiện click lan ra ngoài
                v.setClickable(true);
                v.setFocusable(true);
            });

            // Long-click -> drag
            itemView.setOnLongClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return false;
                int actualPos = pos;
                if (placeholderPosition != -1 && pos > placeholderPosition) actualPos--;

                Task drag = tasks.get(actualPos);
                DraggedTaskInfo info = new DraggedTaskInfo(drag, parentPhase, actualPos, v.getWidth(), v.getHeight());
                v.startDrag(null, new View.DragShadowBuilder(v), info, 0);
                tasks.remove(actualPos);
                notifyItemRemoved(pos);
                return true;
            });
        }
    }

    class PlaceholderViewHolder extends RecyclerView.ViewHolder {
        public PlaceholderViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    // Helpers
    private void bindDate(TaskViewHolder h, Task t) {
        if (t.getDueDate() != null) {
            h.layoutInfoRow.setVisibility(View.VISIBLE);
            h.tvDateRange.setText(ParseDateUtil.toCustomDateTime(t.getDueDate()));
        } else {
            h.layoutInfoRow.setVisibility(View.GONE);
        }
    }

    private void bindCommentAndFile(TaskViewHolder h, Task t) {
        boolean hasC = !t.getComments().isEmpty();
        boolean hasF = !t.getFiles().isEmpty();
        if (!hasC && !hasF) {
            h.layoutInfoRow1.setVisibility(View.GONE);
        } else {
            h.layoutInfoRow1.setVisibility(View.VISIBLE);
            h.imgComment.setVisibility(hasC ? View.VISIBLE : View.GONE);
            h.tvCommentCount.setText(String.valueOf(t.getComments().size()));
            h.imgfile.setVisibility(hasF ? View.VISIBLE : View.GONE);
            h.tvFileCnt.setText(String.valueOf(t.getFiles().size()));
        }
    }

    private void bindAvatar(TaskViewHolder h, Task t) {
        Log.d("TASKADAPTER:", String.valueOf(t.getAssignedTo()));
        if (t.getAssignedTo() != 0) {
            h.layoutInfoRow2.setVisibility(View.VISIBLE);
            // Lấy danh sách thành viên từ ProjectMemberHolder
            List<ProjectMember> members = ProjectMemberHolder.get().getMembers();
            // Tìm user được giao task
            for (ProjectMember member : members) {
                Log.d("TASKADAPTER:", member.getUser().getFullname());
                if (member.getUser().getId() == t.getAssignedTo()) {
                    // Lấy tên người dùng
                    String fullName = member.getUser().getFullname();
                    // Lấy chữ cái đầu của họ và tên
                    String[] nameParts = fullName.split(" ");
                    String initials = "";
                    if (nameParts.length > 0) {
                        initials = nameParts[0].substring(0, 1);
                        if (nameParts.length > 1) {
                            initials += nameParts[nameParts.length - 1].substring(0, 1);
                        }
                    }
                    h.tvAvatar.setText(initials.toUpperCase());
                    break;
                }
            }
        } else {
            h.layoutInfoRow2.setVisibility(View.GONE);
        }
    }

    private void binDescription(TaskViewHolder h, Task t) {
        if (t.getTaskDescription() != null && !t.getTaskDescription().isEmpty() && !t.getTaskDescription().equals("null")) {
            h.tvTaskDes.setVisibility(View.VISIBLE);
            h.tvTaskDes.setText(t.getTaskDescription());
        } else {
            h.tvTaskDes.setVisibility(View.GONE);
        }
    }

    private void updateCardStroke(TaskViewHolder h, boolean checked) {
        int color = ContextCompat.getColor(h.itemView.getContext(),
                checked ? R.color.card_stroke_checked : R.color.card_stroke_default);
        h.cardTask.setStrokeColor(color);
    }
}
