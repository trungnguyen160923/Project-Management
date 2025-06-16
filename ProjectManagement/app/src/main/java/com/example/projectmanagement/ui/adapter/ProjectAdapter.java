package com.example.projectmanagement.ui.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.utils.ParseDateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.graphics.Color;
import com.example.projectmanagement.ui.home.HomeViewModel;
import com.example.projectmanagement.utils.UserPreferences;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {
    private List<Project> projects;
    private HomeViewModel viewModel;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Project project);
    }

    public ProjectAdapter(List<Project> projects, HomeViewModel viewModel, OnItemClickListener listener) {
        this.projects = projects;
        this.viewModel = viewModel;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projects.get(position);
        holder.bind(project);
    }

    @Override
    public int getItemCount() {
        return projects != null ? projects.size() : 0;
    }

    public void updateProjects(List<Project> newProjects) {
        this.projects = newProjects;
        notifyDataSetChanged();
    }

    class ProjectViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvProjectName;
        private final TextView tvTaskCount;
        private final TextView tvCreatedBy;
        private final TextView tvDescription;
        private final TextView tvDeadline;
        private final ImageView ivItemBackground;

        private final MaterialCardView card;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            tvTaskCount = itemView.findViewById(R.id.tvTaskCount);
            tvCreatedBy = itemView.findViewById(R.id.tvCreatedBy);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
            ivItemBackground = itemView.findViewById(R.id.ivItemBackground);
            card = itemView.findViewById(R.id.cardProject);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(projects.get(position));
                }
            });
        }

        public void bind(Project project) {
            tvProjectName.setText(project.getProjectName());
            if(project.getProjectDescription() != null && !project.getProjectDescription().isEmpty()){
                tvDescription.setVisibility(View.VISIBLE);
                tvDescription.setText(project.getProjectDescription());
            }else{
                tvDescription.setVisibility(View.GONE);
            }

            // Get project creator
            viewModel.getProjectCreator(project.getProjectID(), creatorName -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    UserPreferences userPreferences = new UserPreferences(itemView.getContext());
                    if(creatorName.equals(userPreferences.getUser().getFullname())){
                        creatorName += "(Bạn)";
                    }
                    tvCreatedBy.setText("Được tạo bởi: " + creatorName);
                }
            });

            viewModel.getProjectTaskCounts(project.getProjectID(), new HomeViewModel.TaskCountCallback() {
                @Override
                public void onTaskCountsReceived(int completed, int total) {
                    // 1. cập nhật text
                    if (total > 0) {
                        tvTaskCount.setText(completed + "/" + total + " tasks");
                    } else {
                        tvTaskCount.setText("No tasks");
                    }

                    // 2. tính overdue & allDone
                    Date now = new Date();
                    Date dl  = project.getDeadline();
                    boolean isOverdue = dl != null && dl.before(now);
                    boolean allDone   = total > 0 && completed == total;

                    // 3. set viền ngay trong callback
                    if ((dl == null && allDone) || (isOverdue && allDone)) {
                        card.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.completed_green));
                    } else if (isOverdue) {
                        card.setStrokeColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark));
                    } else {
                        card.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.colorAccent));
                    }
                }
            });


            Date now = new Date();
            Date dl = project.getDeadline();
            boolean isOverdue = (dl != null) && dl.before(now);
            // Set deadline
            if (project.getDeadline() != null) {
                String deadline = "Deadline: " + ParseDateUtil.toCustomDateTime(project.getDeadline());
                if(isOverdue){
                    deadline += " (Quá hạn)";
                }
                tvDeadline.setText(deadline);
            }
            else {
                tvDeadline.setText("No deadline");
            }

            // Set background image
            String bg = (project.getBackgroundImg() != null
                    && !project.getBackgroundImg().isEmpty())? project.getBackgroundImg(): "COLOR;#0C90F1";
            Log.d("CHECK=>>>>>>>", bg);
            int radius = ivItemBackground.getContext()
                    .getResources()
                    .getDimensionPixelSize(R.dimen.corner_radius);
            if (bg.startsWith("http")) {
                Glide.with(ivItemBackground.getContext())
                        .load(bg)
                        .transform(new RoundedCorners(radius))
                        .into(ivItemBackground);

            } else if (bg.startsWith("COLOR;")) {
                // tạo Drawable có bo góc luôn
                GradientDrawable gd = new GradientDrawable();
                gd.setColor(Color.parseColor(bg.split(";",2)[1]));
                gd.setCornerRadius(radius);
                ivItemBackground.setBackground(gd);
            } else if (bg.startsWith("GRADIENT;")) {
                String[] parts = bg.split(";",3);
                String[] cols  = parts[1].split(",");
                int c1 = Color.parseColor(cols[0]);
                int c2 = Color.parseColor(cols[1]);
                int ori = Integer.parseInt(parts[2]);
                GradientDrawable gd = new GradientDrawable(
                        GradientDrawable.Orientation.values()[ori],
                        new int[]{c1,c2});
                gd.setCornerRadius(radius);
                ivItemBackground.setBackground(gd);
            }
        }
    }
}
