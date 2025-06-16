package com.example.projectmanagement.ui.home;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.data.model.User;
import com.example.projectmanagement.data.repository.ProjectRepository;
import com.example.projectmanagement.data.service.ProjectMemberService;
import com.example.projectmanagement.data.service.TaskService;
import com.example.projectmanagement.data.service.UserService;
import com.example.projectmanagement.data.service.PhaseService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {
    private static final String TAG = "HomeViewModel";
    private final MutableLiveData<List<Project>> projects = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingState = new MutableLiveData<>(false);
    private ProjectRepository projectRepository;
    private Context context;

    public void init(Context context) {
        Log.d(TAG, "Initializing HomeViewModel");
        this.context = context;
        projectRepository = ProjectRepository.getInstance(context);
        loadProjects();
    }

    public void refresh() {
        Log.d(TAG, "Refreshing projects");
        loadProjects();
    }

    private void loadProjects() {
        Log.d(TAG, "Loading projects");
        loadingState.setValue(true);
        projects.setValue(new ArrayList<>()); // Reset projects list
        errorMessage.setValue(null); // Reset error message
        
        projectRepository.getProjects().observeForever(projectsList -> {
            if (projectsList != null) {
                Log.d(TAG, "Received projects: " + projectsList.size());
                projects.setValue(projectsList);
            } else {
                Log.w(TAG, "Received null projects list");
                projects.setValue(new ArrayList<>());
            }
            loadingState.setValue(false);
        });

        projectRepository.getMessage().observeForever(message -> {
            if (message != null) {
                Log.e(TAG, "Error loading projects: " + message);
                errorMessage.setValue(message);
            }
            loadingState.setValue(false);
        });
    }

    public LiveData<List<Project>> getProjects() {
        return projects;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getLoadingState() {
        return loadingState;
    }

    // Get project creator info
    public void getProjectCreator(int projectId, ProjectCreatorCallback callback) {
        ProjectMemberService.fetchProjectMembers(context, projectId, response -> {
            try {
                if ("success".equals(response.optString("status"))) {
                    JSONArray members = response.optJSONArray("data");
                    if (members != null && members.length() > 0) {
                        // Find admin member (usually the creator)
                        for (int i = 0; i < members.length(); i++) {
                            JSONObject member = members.getJSONObject(i);
                            if ("Admin".equals(member.optString("role"))) {
                                // Get user info
                                int userId = member.optInt("userId");
                                UserService.getUser(context, userId, userResponse -> {
                                    try {
                                        if ("success".equals(userResponse.optString("status"))) {
                                            JSONObject userData = userResponse.optJSONObject("data");
                                            String creatorName = userData.optString("fullname", "Unknown");
                                            callback.onCreatorReceived(creatorName);
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error parsing user data", e);
                                        callback.onCreatorReceived("Unknown");
                                    }
                                }, error -> {
                                    Log.e(TAG, "Error getting user info", error);
                                    callback.onCreatorReceived("Unknown");
                                });
                                return;
                            }
                        }
                        // If no admin found, use first member
                        JSONObject firstMember = members.getJSONObject(0);
                        int userId = firstMember.optInt("userId");
                        UserService.getUser(context, userId, userResponse -> {
                            try {
                                if ("success".equals(userResponse.optString("status"))) {
                                    JSONObject userData = userResponse.optJSONObject("data");
                                    String creatorName = userData.optString("fullname", "Unknown");
                                    callback.onCreatorReceived(creatorName);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing user data", e);
                                callback.onCreatorReceived("Unknown");
                            }
                        }, error -> {
                            Log.e(TAG, "Error getting user info", error);
                            callback.onCreatorReceived("Unknown");
                        });
                    } else {
                        callback.onCreatorReceived("Unknown");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing project members", e);
                callback.onCreatorReceived("Unknown");
            }
        }, error -> {
            Log.e(TAG, "Error getting project members", error);
            callback.onCreatorReceived("Unknown");
        });
    }

    // Get project task counts
    public void getProjectTaskCounts(int projectId, TaskCountCallback callback) {
        if (projectId <= 0) {
            Log.e(TAG, "Invalid project ID");
            callback.onTaskCountsReceived(0, 0);
            return;
        }

        // First get all phases of the project
        PhaseService.getProjectPhases(context, projectId, response -> {
            try {
                if ("success".equals(response.optString("status"))) {
                    JSONArray phases = response.optJSONArray("data");
                    if (phases != null && phases.length() > 0) {
                        final int[] totalTasks = {0};
                        final int[] completedTasks = {0};
                        final int[] processedPhases = {0};
                        
                        // For each phase, get its tasks
                        for (int i = 0; i < phases.length(); i++) {
                            JSONObject phase = phases.getJSONObject(i);
                            int phaseId = phase.optInt("id");
                            
                            TaskService.getTasksByPhaseId(context, phaseId,
                                taskResponse -> {
                                    try {
                                        if ("success".equals(taskResponse.optString("status"))) {
                                            JSONArray tasks = taskResponse.optJSONArray("data");
                                            if (tasks != null) {
                                                totalTasks[0] += tasks.length();
                                                
                                                // Count completed tasks in this phase
                                                for (int j = 0; j < tasks.length(); j++) {
                                                    JSONObject task = tasks.getJSONObject(j);
                                                    if ("DONE".equals(task.optString("status"))) {
                                                        completedTasks[0]++;
                                                    }
                                                }
                                            }
                                        }
                                        
                                        // Increment processed phases counter
                                        processedPhases[0]++;
                                        
                                        // If all phases have been processed, return the final counts
                                        if (processedPhases[0] == phases.length()) {
                                            Log.d(TAG, String.format("Task counts - Completed: %d, Total: %d", 
                                                completedTasks[0], totalTasks[0]));
                                            callback.onTaskCountsReceived(completedTasks[0], totalTasks[0]);
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error parsing tasks for phase " + phaseId, e);
                                        processedPhases[0]++;
                                        if (processedPhases[0] == phases.length()) {
                                            callback.onTaskCountsReceived(completedTasks[0], totalTasks[0]);
                                        }
                                    }
                                },
                                error -> {
                                    Log.e(TAG, "Error getting tasks for phase " + phaseId + "| error: " + error);
                                    processedPhases[0]++;
                                    if (processedPhases[0] == phases.length()) {
                                        callback.onTaskCountsReceived(completedTasks[0], totalTasks[0]);
                                    }
                                }
                            );
                        }
                    } else {
                        Log.w(TAG, "No phases found for project " + projectId);
                        callback.onTaskCountsReceived(0, 0);
                    }
                } else {
                    String errorMessage = response.optString("message", "Unknown error");
                    Log.e(TAG, "Failed to get phases: " + errorMessage);
                    callback.onTaskCountsReceived(0, 0);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing phases", e);
                callback.onTaskCountsReceived(0, 0);
            }
        }, error -> {
            Log.e(TAG, "Error getting phases: " + error.getMessage(), error);
            callback.onTaskCountsReceived(0, 0);
        });
    }

    // Callback interfaces
    public interface ProjectCreatorCallback {
        void onCreatorReceived(String creatorName);
    }

    public interface TaskCountCallback {
        void onTaskCountsReceived(int completedTasks, int totalTasks);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "HomeViewModel cleared");
        if (projectRepository != null) {
            projectRepository.getProjects().removeObserver(projectsList -> {});
            projectRepository.getMessage().removeObserver(message -> {});
        }
    }
}
