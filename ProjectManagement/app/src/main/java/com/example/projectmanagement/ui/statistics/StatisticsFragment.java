package com.example.projectmanagement.ui.statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.Statistics;

public class StatisticsFragment extends Fragment {
    private StatisticsViewModel viewModel;
    private TextView tvOwnedProjects, tvJoinedProjects, tvTotalProjects;
    private TextView tvTotalTasks, tvCompletedTasks, tvPendingTasks;
    private RecyclerView rvProjectMembers, rvPhases;
    private ProgressBar progressBar;
    private ProjectMemberAdapter memberAdapter;
    private PhaseAdapter phaseAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                            ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_statistics, container, false);
        
        // Initialize views
        tvOwnedProjects = root.findViewById(R.id.tv_owned_projects);
        tvJoinedProjects = root.findViewById(R.id.tv_joined_projects);
        tvTotalProjects = root.findViewById(R.id.tv_total_projects);
        tvTotalTasks = root.findViewById(R.id.tv_total_tasks);
        tvCompletedTasks = root.findViewById(R.id.tv_completed_tasks);
        tvPendingTasks = root.findViewById(R.id.tv_pending_tasks);
        rvProjectMembers = root.findViewById(R.id.rv_project_members);
        rvPhases = root.findViewById(R.id.rv_phases);
        progressBar = root.findViewById(R.id.progress_bar);

        // Setup RecyclerViews
        rvProjectMembers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPhases.setLayoutManager(new LinearLayoutManager(getContext()));
        memberAdapter = new ProjectMemberAdapter();
        phaseAdapter = new PhaseAdapter();
        rvProjectMembers.setAdapter(memberAdapter);
        rvPhases.setAdapter(phaseAdapter);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
        
        // Observe data
        viewModel.getStatistics().observe(getViewLifecycleOwner(), this::updateUI);
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> 
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE));
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        // Load data
        viewModel.loadStatistics();

        return root;
    }

    private void updateUI(Statistics statistics) {
        if (statistics == null) return;

        // Update project counts
        tvOwnedProjects.setText(String.valueOf(statistics.getOwnedProjectsCount()));
        tvJoinedProjects.setText(String.valueOf(statistics.getJoinedProjectsCount()));
        tvTotalProjects.setText(String.valueOf(statistics.getTotalProjectsCount()));

        // Update task counts
        tvTotalTasks.setText(String.valueOf(statistics.getTotalTasksCount()));
        tvCompletedTasks.setText(String.valueOf(statistics.getCompletedTasksCount()));
        tvPendingTasks.setText(String.valueOf(statistics.getPendingTasksCount()));

        // Update adapters
        memberAdapter.setProjectMemberStats(statistics.getProjectMemberStats());
        phaseAdapter.setPhaseStats(statistics.getPhaseStats());
    }
}