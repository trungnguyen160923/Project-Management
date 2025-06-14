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

public class PhaseAdapter extends RecyclerView.Adapter<PhaseAdapter.ViewHolder> {
    private List<Statistics.PhaseStat> phaseStats = new ArrayList<>();

    public void setPhaseStats(List<Statistics.PhaseStat> stats) {
        this.phaseStats = stats != null ? stats : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_phase_stat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Statistics.PhaseStat stat = phaseStats.get(position);
        holder.tvProjectName.setText(stat.getProjectName());
        holder.tvPhaseCount.setText(String.valueOf(stat.getPhaseCount()));
    }

    @Override
    public int getItemCount() {
        return phaseStats.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProjectName;
        TextView tvPhaseCount;

        ViewHolder(View view) {
            super(view);
            tvProjectName = view.findViewById(R.id.tv_project_name);
            tvPhaseCount = view.findViewById(R.id.tv_phase_count);
        }
    }
} 