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
        // Check if already navigating
        if (isNavigating) {
            Log.d(TAG, "Already navigating, ignoring click");
            return;
        }

        Log.d(TAG, "Project clicked: " + project.getProjectName() + " (ID: " + project.getProjectID() + ")");
        
        // Set navigating flag
        isNavigating = true;
        
        // Show loading dialog
        Activity activity = getActivity();
        if (activity != null && !activity.isFinishing()) {
            LoadingDialog loadingDialog = new LoadingDialog(activity);
            loadingDialog.show();
            
            // Get project details
            projectRepository.getProject(project.getProjectID()).observe(getViewLifecycleOwner(), projectDetail -> {
                if (projectDetail != null) {
                    Log.d(TAG, "Project details loaded: " + projectDetail.getProjectName());
                    
                    // Get project members
                    TaskService.getProjectMembers(requireContext(), projectDetail.getProjectID(),
                        response -> {
                            try {
                                if ("success".equals(response.optString("status"))) {
                                    List<ProjectMember> members = new ArrayList<>();
                                    for (int i = 0; i < response.getJSONArray("data").length(); i++) {
                                        JSONObject obj = response.getJSONArray("data").getJSONObject(i);
                                        int userId = obj.optInt("userId");
                                        
                                        // Get user details
                                        TaskService.getUserInfo(requireContext(), userId,
                                            userResponse -> {
                                                try {
                                                    if ("success".equals(userResponse.optString("status"))) {
                                                        JSONObject userData = userResponse.getJSONObject("data");
                                                        User user = new User();
                                                        user.setId(userData.optInt("id"));
                                                        user.setUsername(userData.optString("username"));
                                                        user.setEmail(userData.optString("email"));
                                                        user.setFullname(userData.optString("fullname"));
                                                        user.setAvatar(userData.optString("avatar"));
                                                        
                                                        ProjectMember member = new ProjectMember(
                                                            projectDetail.getProjectID(),
                                                            obj.optInt("memberId"),
                                                            userId,
                                                            new Date(),
                                                            ProjectMember.Role.valueOf(obj.optString("role"))
                                                        );
                                                        member.setUser(user);
                                                        members.add(member);
                                                        
                                                        // Store members in ProjectMemberHolder
                                                        ProjectMemberHolder.get().setMembers(members);
                                                    }
                                                } catch (JSONException e) {
                                                    Log.e(TAG, "Error parsing user data", e);
                                                }
                                            },
                                            error -> Log.e(TAG, "Error fetching user info", error)
                                        );
                                    }
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "Error parsing project members", e);
                            }
                        },
                        error -> Log.e(TAG, "Error fetching project members", error)
                    );
                    
                    // Get project phases
                    ProjectService.getProjectPhases(requireContext(), 
                        String.valueOf(projectDetail.getProjectID()),
                        response -> {
                            try {
                                List<Phase> phases = ProjectService.parsePhasesList(response);
                                projectDetail.setPhases(phases);
                                Log.d(TAG, "Loaded " + phases.size() + " phases");

                                // If no phases, just navigate to ProjectActivity
                                if (phases == null || phases.isEmpty()) {
                                    Log.d(TAG, "No phases found, navigating to ProjectActivity");
                                    loadingDialog.dismiss();
                                    ProjectHolder.clear();
                                    navigateToProjectActivity();
                                    return;
                                }

                                // Load tasks for each phase
                                int[] loadedPhases = {0};
                                for (Phase phase : phases) {
                                    ProjectService.getPhaseTasks(requireContext(),
                                        String.valueOf(phase.getPhaseID()),
                                        taskResponse -> {
                                            try {
                                                List<Task> tasks = ProjectService.parseTasksList(taskResponse);
                                                phase.setTasks(tasks);
                                                Log.d(TAG, "Loaded " + tasks.size() + 
                                                    " tasks for phase " + phase.getPhaseName());
                                                
                                                loadedPhases[0]++;
                                                if (loadedPhases[0] == phases.size()) {
                                                    // All phases and tasks loaded
                                                    loadingDialog.dismiss();
                                                    
                                                    // Store project in ProjectHolder
                                                    ProjectHolder.set(projectDetail);
                                                    
                                                    // Navigate to ProjectActivity
                                                    navigateToProjectActivity();
                                                }
                                            } catch (JSONException e) {
                                                Log.e(TAG, "Error parsing tasks", e);
                                                handleError(loadingDialog, "Error loading tasks");
                                            }
                                        },
                                        error -> {
                                            Log.e(TAG, "Error loading tasks", error);
                                            handleError(loadingDialog, "Error loading tasks");
                                        });
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "Error parsing phases", e);
                                handleError(loadingDialog, "Error loading phases");
                            }
                        },
                        error -> {
                            Log.e(TAG, "Error loading phases", error);
                            handleError(loadingDialog, "Error loading phases");
                        });
                } else {
                    Log.e(TAG, "Failed to load project details");
                    handleError(loadingDialog, "Failed to load project details");
                }
            });
        }
    }

    private void navigateToProjectActivity() {
        if (!isNavigating) return; // Double check
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
