package com.example.projectmanagement.ui.project.vm;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.data.model.Task;
import com.example.projectmanagement.data.model.User;
import com.example.projectmanagement.data.service.ProjectService;
import com.example.projectmanagement.data.service.UserService;
import com.example.projectmanagement.utils.ParseDateUtil;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ProjectStatisticViewModel extends AndroidViewModel {
    private static final String TAG = "ProjectStatisticViewModel";
    private final MutableLiveData<Project> project = new MutableLiveData<>();
    private final MutableLiveData<List<Phase>> phases = new MutableLiveData<>();
    private final MutableLiveData<List<Task>> tasks = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private boolean isLoadingData = false;
    private MutableLiveData<List<MemberTaskStats>> memberStatsLiveData = new MutableLiveData<>();

    public ProjectStatisticViewModel(@NonNull Application application) {
        super(application);
        phases.setValue(new ArrayList<>());
        tasks.setValue(new ArrayList<>());
    }

    public LiveData<Project> getProject() {
        return project;
    }

    public LiveData<List<Phase>> getPhases() {
        return phases;
    }

    public LiveData<List<Task>> getTasks() {
        return tasks;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<List<MemberTaskStats>> getMemberStatsLiveData() {
        return memberStatsLiveData;
    }

    public void loadProjectData(int projectId) {
        if (isLoadingData) {
            return;
        }
        isLoadingData = true;
        isLoading.setValue(true);
        error.setValue(null);

        // Load project details
        ProjectService.getProject(getApplication(), String.valueOf(projectId), response -> {
            try {
                Log.d(TAG, "API Response: " + response.toString());
                if ("success".equals(response.optString("status"))) {
                    JSONObject data = response.optJSONObject("data");
                    Log.d(TAG, "Data object: " + (data != null ? data.toString() : "null"));
                    if (data != null) {
                        // Create project object directly from data
                        Project projectData = new Project();
                        projectData.setProjectID(data.optInt("id", -1));
                        projectData.setProjectName(data.optString("projectName", ""));
                        projectData.setProjectDescription(data.optString("description", ""));
                        projectData.setStatus(data.optString("status", ""));

                        // Parse dates
                        try {
                            String startDateStr = data.optString("startDate", "");
                            String endDateStr = data.optString("endDate", "");
                            String createdAtStr = data.optString("createdAt", "");
                            String updatedAtStr = data.optString("updatedAt", "");

                            if (!startDateStr.isEmpty()) {
                                projectData.setStartDate(ParseDateUtil.parseFlexibleIsoDate(startDateStr));
                            }
                            if (!endDateStr.isEmpty()) {
                                projectData.setDeadline(ParseDateUtil.parseFlexibleIsoDate(endDateStr));
                            }
                            if (!createdAtStr.isEmpty()) {
                                projectData.setCreateAt(ParseDateUtil.parseFlexibleIsoDate(createdAtStr));
                            }
                            if (!updatedAtStr.isEmpty()) {
                                projectData.setUpdateAt(ParseDateUtil.parseFlexibleIsoDate(updatedAtStr));
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing dates", e);
                        }

                        // Parse owner if available
                        if (data.has("ownerId")) {
                            try {
                                int ownerId = data.optInt("ownerId", -1);
                                if (ownerId != -1) {
                                    User owner = new User();
                                    owner.setId(ownerId);
                                    projectData.setUser(owner);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing owner", e);
                            }
                        }

                        // Initialize empty lists
                        projectData.setPhases(new ArrayList<>());

                        Log.d(TAG, "Parsed project: " + projectData.toString());
                        project.postValue(projectData);

                        // Load phases after getting project
                        loadPhases(projectId);
                    } else {
                        Log.e(TAG, "Data object is null in response");
                        error.postValue("No project data found");
                        isLoading.setValue(false);
                        isLoadingData = false;
                    }
                } else {
                    String message = response.optString("message", "Unknown error");
                    Log.e(TAG, "API returned error: " + message);
                    error.postValue("Error: " + message);
                    isLoading.setValue(false);
                    isLoadingData = false;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing project", e);
                error.postValue("Error loading project: " + e.getMessage());
                isLoading.setValue(false);
                isLoadingData = false;
            }
        }, volleyError -> {
            Log.e(TAG, "Error loading project", volleyError);
            if (volleyError.networkResponse != null) {
                try {
                    String errorBody = new String(volleyError.networkResponse.data, "UTF-8");
                    Log.e(TAG, "Error response body: " + errorBody);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing error response", e);
                }
            }
            error.postValue(volleyError.getMessage());
            isLoading.setValue(false);
            isLoadingData = false;
        });
    }

    private void loadPhases(int projectId) {
        Log.d(TAG, "Loading phases for project: " + projectId);

        ProjectService.getProjectPhases(getApplication(), String.valueOf(projectId),
            response -> {
                try {
                    List<Phase> phaseList = ProjectService.parsePhasesList(response);
                    Log.d(TAG, "Loaded " + phaseList.size() + " phases");

                    // Initialize tasks list
                    final List<Task> allTasks = new ArrayList<>();
                    final int[] loadedPhases = {0};
                    final int totalPhases = phaseList.size();

                    if (totalPhases == 0) {
                        phases.postValue(phaseList);
                        tasks.postValue(allTasks);
                        isLoading.setValue(false);
                        isLoadingData = false;
                        return;
                    }

                    // Load tasks for each phase sequentially
                    loadTasksForPhase(phaseList, 0, allTasks, loadedPhases, totalPhases);

                    // Set phases immediately
                    phases.postValue(phaseList);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing phases", e);
                    error.postValue("Error loading phases: " + e.getMessage());
                    isLoading.setValue(false);
                    isLoadingData = false;
                }
            },
            error -> {
                Log.e(TAG, "Error loading phases", error);
                this.error.postValue("Error loading phases: " + error.getMessage());
                isLoading.setValue(false);
                isLoadingData = false;
            }
        );
    }

    private void loadTasksForPhase(List<Phase> phases, int currentIndex, List<Task> allTasks,
                                 int[] loadedPhases, int totalPhases) {
        if (currentIndex >= phases.size()) {
            return;
        }

        Phase phase = phases.get(currentIndex);
        Log.d(TAG, "Loading tasks for phase: " + phase.getPhaseID());

        ProjectService.getPhaseTasks(getApplication(), String.valueOf(phase.getPhaseID()),
            response -> {
                try {
                    List<Task> phaseTasks = ProjectService.parseTasksList(response);
                    Log.d(TAG, "Loaded " + phaseTasks.size() + " tasks for phase " + phase.getPhaseID());

                    // Add tasks to phase
                    phase.setTasks(phaseTasks);

                    // Add tasks to all tasks list
                    allTasks.addAll(phaseTasks);

                    // Update tasks list
                    tasks.postValue(allTasks);

                    // Increment loaded phases counter
                    loadedPhases[0]++;

                    // Check if all phases are loaded
                    if (loadedPhases[0] == totalPhases) {
                        isLoading.setValue(false);
                        isLoadingData = false;
                    } else {
                        // Load next phase
                        loadTasksForPhase(phases, currentIndex + 1, allTasks, loadedPhases, totalPhases);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing tasks for phase " + phase.getPhaseID(), e);
                    error.postValue("Error loading tasks: " + e.getMessage());

                    // Continue with next phase even if there's an error
                    loadedPhases[0]++;
                    if (loadedPhases[0] == totalPhases) {
                        isLoading.setValue(false);
                        isLoadingData = false;
                    } else {
                        loadTasksForPhase(phases, currentIndex + 1, allTasks, loadedPhases, totalPhases);
                    }
                }
            },
            error -> {
                Log.e(TAG, "Error loading tasks for phase " + phase.getPhaseID(), error);
                this.error.postValue("Error loading tasks: " + error.getMessage());

                // Continue with next phase even if there's an error
                loadedPhases[0]++;
                if (loadedPhases[0] == totalPhases) {
                    isLoading.setValue(false);
                    isLoadingData = false;
                } else {
                    loadTasksForPhase(phases, currentIndex + 1, allTasks, loadedPhases, totalPhases);
                }
            }
        );
    }

    public void refreshData() {
        Project currentProject = project.getValue();
        if (currentProject != null) {
            loadProjectData(currentProject.getProjectID());
        }
    }

    // Helper methods for statistics
    public Map<String, Integer> getTaskStatusCounts() {
        List<Task> taskList = tasks.getValue();
        if (taskList == null) return new HashMap<>();

        Map<String, Integer> statusCounts = new HashMap<>();
        statusCounts.put("completed", 0);
        statusCounts.put("overdue", 0);
        statusCounts.put("pending", 0);

        Date currentDate = new Date();

        for (Task task : taskList) {

            // Kiểm tra task có quá hạn không
            boolean isOverdue = false;
            Date deadline = (task.getDueDate());

            if (deadline != null) {

                if (currentDate.after(deadline)) {
                    isOverdue = true;
                }
            } else {
                Log.e("CHECKDATE", "Failed to parse deadline for task " + task.getTaskID() + ": " + deadline);
            }

            // Kiểm tra trạng thái hoàn thành
            if (task.getStatus().equals("DONE")) {
                statusCounts.put("completed", statusCounts.get("completed") + 1);
            } else if (isOverdue) {
                statusCounts.put("overdue", statusCounts.get("overdue") + 1);
            } else {
                statusCounts.put("pending", statusCounts.get("pending") + 1);
            }
        }


        return statusCounts;
    }

    public static class MemberTaskStats {
        public int memberId;
        public String memberName;
        public int totalTasks;
        public int completedTasks;

        public MemberTaskStats(int memberId) {
            this.memberId = memberId;
            this.memberName = "Member " + memberId; // Default name
            this.totalTasks = 0;
            this.completedTasks = 0;
        }
    }

    public void loadMemberStatistics() {
        List<Task> taskList = tasks.getValue();
        if (taskList == null) {
            memberStatsLiveData.setValue(new ArrayList<>());
            return;
        }

        Map<Integer, MemberTaskStats> memberStats = new HashMap<>();
        Set<Integer> memberIds = new HashSet<>();

        // First pass: collect all member IDs and initialize stats
        for (Task task : taskList) {
            int assigneeId = task.getAssignedTo();
            if (assigneeId != 0) {
                memberIds.add(assigneeId);
                memberStats.putIfAbsent(assigneeId, new MemberTaskStats(assigneeId));
                MemberTaskStats stats = memberStats.get(assigneeId);
                stats.totalTasks++;
                if (task.getStatus().equals("DONE")) {
                    stats.completedTasks++;
                }
            }
        }

        // Get member names
        final int[] pendingRequests = {memberIds.size()};
        if (pendingRequests[0] == 0) {
            // No members to fetch, return current stats
            memberStatsLiveData.setValue(new ArrayList<>(memberStats.values()));
            return;
        }

        for (int memberId : memberIds) {
            UserService.getUser(getApplication(), memberId,
                response -> {
                    try {
                        JSONObject userJson = response.getJSONObject("data");
                        String fullname = userJson.getString("fullname");
                        MemberTaskStats stats = memberStats.get(memberId);
                        if (stats != null) {
                            stats.memberName = fullname;
                        }
                    } catch (JSONException e) {
                        Log.e("MEMBER_STATS", "Error parsing user data for ID " + memberId, e);
                    } finally {
                        pendingRequests[0]--;
                        if (pendingRequests[0] == 0) {
                            // All requests completed, update LiveData
                            memberStatsLiveData.setValue(new ArrayList<>(memberStats.values()));
                        }
                    }
                },
                error -> {
                    Log.e("MEMBER_STATS", "Error getting user name for ID " + memberId + ": " + error.getMessage());
                    pendingRequests[0]--;
                    if (pendingRequests[0] == 0) {
                        // All requests completed, update LiveData
                        memberStatsLiveData.setValue(new ArrayList<>(memberStats.values()));
                    }
                }
            );
        }
    }

    public List<MemberTaskStats> getMemberTaskStatistics() {
        List<Task> taskList = tasks.getValue();
        if (taskList == null) return new ArrayList<>();

        Map<Integer, MemberTaskStats> memberStats = new HashMap<>();
        Set<Integer> memberIds = new HashSet<>();

        // First pass: collect all member IDs and initialize stats
        for (Task task : taskList) {
            int assigneeId = task.getAssignedTo();
            if (assigneeId != 0) {
                memberIds.add(assigneeId);
                memberStats.putIfAbsent(assigneeId, new MemberTaskStats(assigneeId));
                MemberTaskStats stats = memberStats.get(assigneeId);
                stats.totalTasks++;
                if (task.getStatus().equals("DONE")) {
                    stats.completedTasks++;
                }
            }
        }

        // Get member names
        for (int memberId : memberIds) {
            UserService.getUser(getApplication(), memberId,
                response -> {
                    try {
                        JSONObject userJson = response.getJSONObject("data");
                        String fullname = userJson.getString("fullname");
                        MemberTaskStats stats = memberStats.get(memberId);
                        if (stats != null) {
                            stats.memberName = fullname;
                        }
                    } catch (JSONException e) {
                        Log.e("MEMBER_STATS", "Error parsing user data for ID " + memberId, e);
                    }
                },
                error -> {
                    Log.e("MEMBER_STATS", "Error getting user name for ID " + memberId + ": " + error.getMessage());
                }
            );
        }

        return new ArrayList<>(memberStats.values());
    }
}
