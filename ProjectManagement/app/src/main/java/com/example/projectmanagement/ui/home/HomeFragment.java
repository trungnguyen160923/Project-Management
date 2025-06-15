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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.projectmanagement.data.service.ProjectService;
import com.example.projectmanagement.data.model.ProjectHolder;
import com.example.projectmanagement.data.service.UserService;
import com.example.projectmanagement.databinding.FragmentHomeBinding;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.ui.adapter.ProjectAdapter;
import com.example.projectmanagement.ui.project.ProjectActivity;
import com.example.projectmanagement.data.repository.ProjectRepository;
import com.example.projectmanagement.ui.project.vm.ProjectViewModel;
import com.example.projectmanagement.utils.Helpers;
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
    private LoadingDialog loadingDialog;

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
        setupRecyclerView();

        // Khởi tạo ProjectRepository
        projectRepository = ProjectRepository.getInstance(requireContext());

        // Setup SwipeRefreshLayout
        setupSwipeRefresh();
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "Swipe refresh triggered");
            viewModel.refresh();
        });
        binding.swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        );

        // Observe loading state
        viewModel.getLoadingState().observe(getViewLifecycleOwner(), isLoading -> {
            Log.d(TAG, "Loading state changed: " + isLoading);
            binding.swipeRefreshLayout.setRefreshing(isLoading);
        });
    }

    private void setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView");
        binding.rvProjects.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProjectAdapter(new ArrayList<>(), viewModel, this::onItemClick);
        binding.rvProjects.setAdapter(adapter);

        // Observe projects
        viewModel.getProjects().observe(getViewLifecycleOwner(), projects -> {
            Log.d(TAG, "Projects updated: " + (projects != null ? projects.size() : 0) + " projects");
            adapter.updateProjects(projects);
            
            // Update visibility on main thread
            if (getActivity() != null && !getActivity().isFinishing()) {
                getActivity().runOnUiThread(() -> {
                    if (projects != null && !projects.isEmpty()) {
                        Log.d(TAG, "Showing RecyclerView, hiding empty view");
                        binding.rvProjects.setVisibility(View.VISIBLE);
                        binding.emptyView.setVisibility(View.GONE);
                    } else {
                        Log.d(TAG, "Showing empty view, hiding RecyclerView");
                        binding.rvProjects.setVisibility(View.GONE);
                        binding.emptyView.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    @Override
    public void onItemClick(Project project) {
        Log.d(TAG, ">>> project is clicked: " + project);
        // Check if already navigating
        if (isNavigating) {
            Log.d(TAG, ">>> Already navigating, ignoring click");
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
                    Log.d(TAG, ">>> this is project name: " + projectDetail.getProjectName());
                    ProjectHolder.set(projectDetail);
                    fetchPhasesData();
                } else {
                    Log.e(TAG, ">>> Failed to load project details");
                    handleError(loadingDialog, "Failed to load project details");
                }
            });
        }
    }

    private void fetchPhasesData() {
        Log.d(TAG, ">>> fetching phases");
        // Get project phases
        ProjectService.getProjectPhases(requireContext(),
                String.valueOf(ProjectHolder.get().getProjectID()),
                response -> {
                    Log.d(TAG, ">>> fetched phases successfully!");
                    try {
                        List<Phase> phases = ProjectService.parsePhasesList(response);
                        Log.d(TAG, ">>> Loaded: " + phases.size() + " phases");

                        // If no phases, just navigate to ProjectActivity
                        if (phases == null || phases.isEmpty()) {
                            Log.d(TAG, "No phases found, navigating to ProjectActivity");
                            fetchProjectMembersData();
                            return;
                        }

                        // Load tasks for each phase
                        int[] loadedPhases = {0};
                        int totalPhases = phases.size();
                        
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
                                            if (loadedPhases[0] == totalPhases) {
                                                // Tất cả phase đã load xong task
                                                ProjectHolder.get().setPhases(phases);
                                                fetchProjectMembersData();
                                            }
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error parsing tasks", e);
                                            loadedPhases[0]++;
                                            if (loadedPhases[0] == totalPhases) {
                                                ProjectHolder.get().setPhases(phases);
                                                fetchProjectMembersData();
                                            }
                                        }
                                    },
                                    error -> {
                                        Log.e(TAG, "Error loading tasks", error);
                                        loadedPhases[0]++;
                                        if (loadedPhases[0] == totalPhases) {
                                            ProjectHolder.get().setPhases(phases);
                                            fetchProjectMembersData();
                                        }
                                    });
                        }
                    } catch (Exception e) {
                        Log.e(TAG, ">>> Error parsing phases", e);
                        fetchProjectMembersData();
                    }
                },
                error -> {
                    Log.e(TAG, ">>> Error fetching phases", error);
                    fetchProjectMembersData();
                });
    }

    private void fetchProjectMembersData() {
        // Get project members
        TaskService.getProjectMembers(requireContext(), ProjectHolder.get().getProjectID(),
                taskResponse -> {
                    try {
                        if ("success".equals(taskResponse.optString("status"))) {
                            List<ProjectMember> members = new ArrayList<>();
                            for (int i = 0; i < taskResponse.getJSONArray("data").length(); i++) {
                                JSONObject obj = taskResponse.getJSONArray("data").getJSONObject(i);
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
                                                            ProjectHolder.get().getProjectID(),
                                                            obj.optInt("memberId"),
                                                            userId,
                                                            new Date(),
                                                            ProjectMember.Role.valueOf(obj.optString("role"))
                                                    );
                                                    member.setUser(user);
                                                    members.add(member);
                                                    // Store members in ProjectMemberHolder
                                                    ProjectMemberHolder.get().setMembers(members);

                                                    navigateToProjectActivity();
                                                }
                                            } catch (Exception e) {
                                                Log.e(TAG, "Error parsing user data", e);
                                            }
                                        },
                                        error -> Log.e(TAG, "Error fetching user info", error)
                                );
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing project members", e);
                    }
                },
                error -> {
            String errMsg="kkk";
            try {
                errMsg= Helpers.parseError(error);
            }catch (Exception e){

            }
            Log.d(TAG,">>> err msg at kkkk oooo aaaa: "+errMsg);
                }
        );
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

    @Override
    public void onResume() {
        super.onResume();
        // Refresh project list when returning from MenuProjectActivity
        if (viewModel != null) {
            viewModel.refresh();
        }
    }
}
