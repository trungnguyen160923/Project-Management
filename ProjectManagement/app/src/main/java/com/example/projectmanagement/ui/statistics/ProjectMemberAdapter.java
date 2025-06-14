package com.example.projectmanagement.ui.statistics;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.Statistics;

import java.util.ArrayList;
import java.util.List;

public class ProjectMemberAdapter extends RecyclerView.Adapter<ProjectMemberAdapter.ViewHolder> {
    private List<Statistics.ProjectMemberStat> projectMemberStats = new ArrayList<>();

    public void setProjectMemberStats(List<Statistics.ProjectMemberStat> stats) {
        this.projectMemberStats = stats != null ? stats : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project_member_stat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Statistics.ProjectMemberStat stat = projectMemberStats.get(position);
        holder.tvProjectName.setText(stat.getProjectName());
        holder.tvMemberCount.setText(String.valueOf(stat.getMemberCount()));
    }

    @Override
    public int getItemCount() {
        return projectMemberStats.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProjectName;
        TextView tvMemberCount;

        ViewHolder(View view) {
            super(view);
            tvProjectName = view.findViewById(R.id.tv_project_name);
            tvMemberCount = view.findViewById(R.id.tv_member_count);
        }
    }
} 