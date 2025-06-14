package com.example.projectmanagement.ui.statistics;

import android.graphics.Color;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.Statistics;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class StatisticsFragment extends Fragment {
    private StatisticsViewModel viewModel;
    private TextView tvTotalProjects, tvTaskProgress, tvAssignedTaskProgress;
    private ProgressBar progressBar;
    private PieChart pieChartTasks, pieChartAssignedTasks;
    private BarChart barChartMembers, barChartProjectTasks;
    private SwipeRefreshLayout swipeRefreshLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                            ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_statistics, container, false);
        
        // Initialize views
        tvTotalProjects = root.findViewById(R.id.tvTotalProjects);
        tvTaskProgress = root.findViewById(R.id.tvTaskProgress);
        tvAssignedTaskProgress = root.findViewById(R.id.tvAssignedTaskProgress);
        progressBar = root.findViewById(R.id.progress_bar);
        pieChartTasks = root.findViewById(R.id.pieChartTasks);
        pieChartAssignedTasks = root.findViewById(R.id.pieChartAssignedTasks);
        barChartMembers = root.findViewById(R.id.barChartMembers);
        barChartProjectTasks = root.findViewById(R.id.barChartProjectTasks);
        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);

        // Setup charts
        setupPieChart(pieChartTasks);
        setupPieChart(pieChartAssignedTasks);
        setupBarChart(barChartMembers);
        setupBarChart(barChartProjectTasks);

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
        
        // Observe data
        viewModel.getStatistics().observe(getViewLifecycleOwner(), this::updateUI);
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            swipeRefreshLayout.setRefreshing(isLoading);
        });
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        // Load data
        viewModel.loadStatistics();

        return root;
    }

    private void setupPieChart(PieChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setHoleRadius(50f);
        chart.setTransparentCircleRadius(55f);
        chart.setDrawHoleEnabled(true);
        chart.setRotationAngle(0);
        chart.setRotationEnabled(true);
        chart.setHighlightPerTapEnabled(true);
        chart.animateY(1000);
        chart.getLegend().setEnabled(true);
    }

    private void setupBarChart(BarChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(true);
        chart.animateY(1000);
    }

    private void updatePieChart(PieChart chart, int completed, int total, String title) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(completed, "Đã hoàn thành"));
        entries.add(new PieEntry(total - completed, "Chưa hoàn thành"));

        PieDataSet dataSet = new PieDataSet(entries, title);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        chart.setData(data);
        chart.invalidate();
    }

    private void updateBarChart(BarChart chart, List<BarEntry> entries, List<String> labels, String title) {
        BarDataSet dataSet = new BarDataSet(entries, title);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        BarData data = new BarData(dataSet);
        chart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels));
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setLabelRotationAngle(-45f);
        chart.setData(data);
        chart.invalidate();
    }

    private void refreshData() {
        viewModel.loadStatistics();
    }

    private void updateUI(Statistics statistics) {
        if (statistics == null) return;

        // Update overview
        tvTotalProjects.setText(String.format("Tổng số Project: %d", statistics.getTotalProjectsCount()));
        tvTaskProgress.setText(String.format("Task đã hoàn thành: %d/%d", 
            statistics.getCompletedTasksCount(), statistics.getTotalTasksCount()));
        tvAssignedTaskProgress.setText(String.format("Task được giao đã hoàn thành: %d/%d",
            statistics.getCompletedAssignedTasksCount(), statistics.getAssignedTasksCount()));

        // Update pie charts
        updatePieChart(pieChartTasks, statistics.getCompletedTasksCount(), 
            statistics.getTotalTasksCount(), "Tiến độ Task");
        updatePieChart(pieChartAssignedTasks, statistics.getCompletedAssignedTasksCount(),
            statistics.getAssignedTasksCount(), "Tiến độ Task được giao");

        // Update member bar chart
        List<Statistics.ProjectMemberStat> memberStats = statistics.getProjectMemberStats();
        if (memberStats != null && !memberStats.isEmpty()) {
            List<BarEntry> memberEntries = new ArrayList<>();
            List<String> memberLabels = new ArrayList<>();
            for (int i = 0; i < memberStats.size(); i++) {
                Statistics.ProjectMemberStat stat = memberStats.get(i);
                memberEntries.add(new BarEntry(i, stat.getMemberCount()));
                memberLabels.add(stat.getProjectName());
            }
            updateBarChart(barChartMembers, memberEntries, memberLabels, "Số thành viên");
        } else {
            // Clear the chart if no data
            barChartMembers.clear();
            barChartMembers.invalidate();
        }

        // Update project tasks bar chart
        List<Statistics.ProjectTaskStat> taskStats = statistics.getProjectTaskStats();
        if (taskStats != null && !taskStats.isEmpty()) {
            List<BarEntry> taskEntries = new ArrayList<>();
            List<String> taskLabels = new ArrayList<>();
            for (int i = 0; i < taskStats.size(); i++) {
                Statistics.ProjectTaskStat stat = taskStats.get(i);
                taskEntries.add(new BarEntry(i, stat.getCompletedAssignedTasks()));
                taskLabels.add(stat.getProjectName());
            }
            updateBarChart(barChartProjectTasks, taskEntries, taskLabels, "Task đã hoàn thành theo Project");
        } else {
            // Clear the chart if no data
            barChartProjectTasks.clear();
            barChartProjectTasks.invalidate();
        }
    }
}