package com.example.projectmanagement.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.projectmanagement.data.model.ProjectHolder;
import com.example.projectmanagement.databinding.FragmentHomeBinding;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.ui.adapter.ProjectAdapter;
import com.example.projectmanagement.ui.project.ProjectActivity;
import com.example.projectmanagement.data.repository.ProjectRepository;
import com.example.projectmanagement.utils.LoadingDialog;

import java.io.Serializable;
import java.util.List;

public class HomeFragment extends Fragment implements ProjectAdapter.OnItemClickListener {
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private ProjectAdapter adapter;
    private boolean isNavigating = false;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        viewModel.init(requireContext());

        // Khởi tạo Adapter và gán sự kiện click
        adapter = new ProjectAdapter(this);
        binding.rvProject.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvProject.setAdapter(adapter);

        // Observe dữ liệu dự án
        viewModel.getProjects().observe(getViewLifecycleOwner(), this::renderProjects);
    }

    private void renderProjects(List<Project> projects) {
        boolean isEmpty = projects == null || projects.isEmpty();
        binding.emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvProject.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        if (!isEmpty) {
            adapter.setData(projects);
        }
    }

    @Override
    public void onItemClick(Project project) {
        // Prevent multiple clicks
        if (isNavigating) return;
        isNavigating = true;

        // Hiển thị loading dialog
        LoadingDialog loadingDialog = new LoadingDialog((Activity) requireContext());
        loadingDialog.show();

        // Lấy chi tiết project trước
        ProjectRepository.getInstance(requireContext())
            .getProjectDetail(String.valueOf(project.getProjectID()))
            .observe(getViewLifecycleOwner(), projectData -> {
                loadingDialog.dismiss();
                isNavigating = false;
                if (projectData != null) {
                    // Lưu project vào holder
                    ProjectHolder.set(projectData);
                    // Mở activity
                    startActivity(new Intent(requireContext(), ProjectActivity.class));
                } else {
                    Toast.makeText(requireContext(), "Không thể tải thông tin project", Toast.LENGTH_SHORT).show();
                }
            });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
