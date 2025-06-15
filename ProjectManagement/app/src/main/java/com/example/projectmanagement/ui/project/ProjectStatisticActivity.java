package com.example.projectmanagement.ui.project;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.projectmanagement.R;
import com.example.projectmanagement.ui.project.vm.ProjectStatisticViewModel;
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
import java.util.Map;

public class ProjectStatisticActivity extends AppCompatActivity {
    private ProjectStatisticViewModel viewModel;
    private TextView tvTotalPhases, tvTotalTasks;
    private ProgressBar progressBar;
    private PieChart pieChartTaskStatus;
    private BarChart barChartMemberStats;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static final String TAG = "ProjectStatisticActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_statistic);

        // Setup toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Thống kê dự án");
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize views
        progressBar = findViewById(R.id.progress_bar);
        pieChartTaskStatus = findViewById(R.id.pieChartTaskStatus);
        barChartMemberStats = findViewById(R.id.barChartMemberStats);
        swipeRefreshLayout = findViewById(R.id.main);
        tvTotalPhases = findViewById(R.id.tvTotalPhases);
        tvTotalTasks = findViewById(R.id.tvTotalTasks);

        // Setup charts
        setupPieChart();
        setupBarChart();

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ProjectStatisticViewModel.class);

        // Observe data
        viewModel.getPhases().observe(this, phases -> {
            if (phases != null) {
                tvTotalPhases.setText(String.format("Tổng số Phase: %d", phases.size()));
            }
            updateTaskStatusChart();
            viewModel.loadMemberStatistics(); // Load member stats when phases are loaded
        });
        viewModel.getTasks().observe(this, tasks -> {
            if (tasks != null) {
                tvTotalTasks.setText(String.format("Tổng số Task: %d", tasks.size()));
            }
            updateTaskStatusChart();
            viewModel.loadMemberStatistics(); // Reload member stats when tasks change
        });
        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            swipeRefreshLayout.setRefreshing(isLoading);
        });
        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
        viewModel.getMemberStatsLiveData().observe(this, this::updateMemberStatsChart);

        // Load initial data
        refreshData();
    }

    private void setupPieChart() {
        pieChartTaskStatus.getDescription().setEnabled(false);
        pieChartTaskStatus.setHoleRadius(50f);
        pieChartTaskStatus.setTransparentCircleRadius(55f);
        pieChartTaskStatus.setDrawHoleEnabled(true);
        pieChartTaskStatus.setRotationAngle(0);
        pieChartTaskStatus.setRotationEnabled(true);
        pieChartTaskStatus.setHighlightPerTapEnabled(true);
        pieChartTaskStatus.animateY(1000);
        pieChartTaskStatus.getLegend().setEnabled(true);
    }

    private void setupBarChart() {
        barChartMemberStats.getDescription().setEnabled(false);
        barChartMemberStats.setDrawGridBackground(false);
        barChartMemberStats.setDrawBarShadow(false);
        barChartMemberStats.setDrawValueAboveBar(true);
        barChartMemberStats.getXAxis().setDrawGridLines(false);
        barChartMemberStats.getAxisLeft().setDrawGridLines(true);
        barChartMemberStats.getAxisRight().setEnabled(false);
        barChartMemberStats.getLegend().setEnabled(true);
        barChartMemberStats.animateY(1000);
    }

    private void updateTaskStatusChart() {
        Map<String, Integer> statusCounts = viewModel.getTaskStatusCounts();
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(statusCounts.get("completed"), "Đã hoàn thành"));
        entries.add(new PieEntry(statusCounts.get("overdue"), "Quá hạn"));
        entries.add(new PieEntry(statusCounts.get("pending"), "Chưa hoàn thành"));

        PieDataSet dataSet = new PieDataSet(entries, "Trạng thái Task");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        pieChartTaskStatus.setData(data);
        pieChartTaskStatus.invalidate();
    }

    private void updateMemberStatsChart(List<ProjectStatisticViewModel.MemberTaskStats> memberStats) {
        if (memberStats == null || memberStats.isEmpty()) {
            barChartMemberStats.clear();
            barChartMemberStats.invalidate();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < memberStats.size(); i++) {
            ProjectStatisticViewModel.MemberTaskStats stats = memberStats.get(i);
            entries.add(new BarEntry(i, stats.completedTasks));
            labels.add(stats.memberName);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Task đã hoàn thành");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);
        
        // Set value formatter to show integers
        dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        BarData data = new BarData(dataSet);
        barChartMemberStats.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels));
        barChartMemberStats.getXAxis().setGranularity(1f);
        barChartMemberStats.getXAxis().setLabelRotationAngle(-45f);
        
        // Set Y axis to show only integers
        barChartMemberStats.getAxisLeft().setGranularity(1f);
        barChartMemberStats.getAxisLeft().setDrawGridLines(true);
        barChartMemberStats.getAxisLeft().setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });
        
        barChartMemberStats.setData(data);
        barChartMemberStats.invalidate();
    }

    private void refreshData() {
        int projectId = getIntent().getIntExtra("project_id", -1);
        if (projectId != -1) {
            viewModel.loadProjectData(projectId);
        }
    }
}