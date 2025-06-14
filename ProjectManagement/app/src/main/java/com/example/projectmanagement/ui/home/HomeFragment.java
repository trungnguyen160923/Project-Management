package com.example.projectmanagement.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.projectmanagement.data.service.ProjectService;
import com.example.projectmanagement.data.model.ProjectHolder;
import com.example.projectmanagement.data.service.UserService;
import com.example.projectmanagement.databinding.FragmentHomeBinding;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.ui.adapter.ProjectAdapter;
import com.example.projectmanagement.ui.project.ProjectActivity;
import com.example.projectmanagement.data.repository.ProjectRepository;
import com.example.projectmanagement.utils.LoadingDialog;
import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.data.model.Task;
import com.example.projectmanagement.data.service.TaskService;
import com.example.projectmanagement.data.model.ProjectMember;
import com.example.projectmanagement.data.model.ProjectMemberHolder;
import com.example.projectmanagement.data.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class HomeFragment extends Fragment implements ProjectAdapter.OnItemClickListener {
    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private ProjectAdapter adapter;
    private boolean isNavigating = false;
    private ProjectRepository projectRepository;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        Log.d(TAG, "Creating view");
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "View created");

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        viewModel.init(requireContext());

        // Khởi tạo Adapter và gán sự kiện click
        adapter = new ProjectAdapter(this);
        binding.rvProject.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvProject.setAdapter(adapter);

        // Observe dữ liệu dự án
        viewModel.getProjects().observe(getViewLifecycleOwner(), this::renderProjects);

        // Khởi tạo ProjectRepository
        projectRepository = ProjectRepository.getInstance(requireContext());
    }

    private void renderProjects(List<Project> projects) {
        Log.d(TAG, "Rendering " + (projects != null ? projects.size() : 0) + " projects");
        boolean isEmpty = projects == null || projects.isEmpty();
        binding.emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvProject.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        if (!isEmpty) {
            adapter.setData(projects);
        }
    }

    @Override
    public void onItemClick(Project project) {
        Log.d(TAG, ">>> project is clicked: " + project);
        // Check if already navigating
        if (isNavigating) {
            Log.d(TAG, "Already navigating, ignoring click");
            return;
        }

        // Set navigating flag
        isNavigating = true;

        // Show loading dialog
        Activity activity = getActivity();
        Log.d(TAG, ">>> activity: " + activity);
        if (activity != null && !activity.isFinishing()) {
            LoadingDialog loadingDialog = new LoadingDialog(activity);
            loadingDialog.show();

            // Get project details
            projectRepository.getProject(project.getProjectID()).observe(getViewLifecycleOwner(), projectDetail -> {
                Log.d(TAG, ">>> fetched project successfully!");
                if (projectDetail != null) {
                    Log.d(TAG, ">>> this is project details: " + projectDetail.getProjectName());
                    ProjectHolder.set(projectDetail);
                    navigateToProjectActivity();
                } else {
                    Log.e(TAG, ">>> Failed to load project details");
                    handleError(loadingDialog, "Failed to load project details");
                }
            });
        }
    }

    private void navigateToProjectActivity() {
        if (!isNavigating) return; // Double check
        Log.d(TAG, ">>> Navigating to ProjectActivity");
        Intent intent = new Intent(getActivity(), ProjectActivity.class);
        startActivity(intent);
        isNavigating = false; // Reset flag after navigation
    }

    private void handleError(LoadingDialog loadingDialog, String message) {
        loadingDialog.dismiss();
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        isNavigating = false; // Reset flag on error
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "Destroying view");
        binding = null;
        isNavigating = false; // Reset flag when view is destroyed
    }
}
