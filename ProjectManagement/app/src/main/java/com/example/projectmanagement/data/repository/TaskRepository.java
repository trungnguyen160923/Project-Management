package com.example.projectmanagement.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.constraintlayout.widget.Placeholder;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.projectmanagement.data.service.TaskService;
import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.data.model.ProjectHolder;
import com.example.projectmanagement.data.model.Task;
import com.example.projectmanagement.utils.ParseDateUtil;
import com.example.projectmanagement.data.model.ProjectMember;
import com.example.projectmanagement.data.model.ProjectMemberHolder;
import com.example.projectmanagement.data.model.User;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class TaskRepository {
    private static final String TAG = "TaskRepository";
    private static TaskRepository instance;
    private final Context context;
    private final RequestQueue requestQueue;
    private final MutableLiveData<List<Task>> tasksLiveData;
    private final MutableLiveData<String> messageLiveData;
    private final MutableLiveData<Task> currentTaskLiveData;
    private final MutableLiveData<List<User>> projectMembersLiveData = new MutableLiveData<>(new ArrayList<>());

    private TaskRepository(Context ctx) {
        this.context = ctx.getApplicationContext();
        this.requestQueue = Volley.newRequestQueue(context);
        this.tasksLiveData = new MutableLiveData<>(new ArrayList<>());
        this.messageLiveData = new MutableLiveData<>();
        this.currentTaskLiveData = new MutableLiveData<>();
    }

    public static TaskRepository getInstance(Context ctx) {
        if (instance == null) {
            instance = new TaskRepository(ctx);
        }
        return instance;
    }

    public LiveData<List<Task>> getPhaseTasks(int phaseId) {
        Project currentProject = ProjectHolder.get();
        if (currentProject != null) {
            for (Phase phase : currentProject.getPhases()) {
                if (phase.getPhaseID() == phaseId) {
                    List<Task> tasks = phase.getTasks();
                    if (tasks != null) {
                        tasksLiveData.setValue(tasks);
                        Log.d(TAG, "Loaded " + tasks.size() + " tasks for phase " + phaseId);
                    } else {
                        tasksLiveData.setValue(new ArrayList<>());
                        Log.d(TAG, "No tasks found for phase " + phaseId);
                    }
                    return tasksLiveData;
                }
            }
        }
        tasksLiveData.setValue(new ArrayList<>());
        Log.d(TAG, "Phase " + phaseId + " not found");
        return tasksLiveData;
    }

    public LiveData<String> getMessage() {
        return messageLiveData;
    }

    public LiveData<Task> createTask(Task task) {
        final MutableLiveData<Task> result = new MutableLiveData<>();
        Project currentProject = ProjectHolder.get();
        
        if (currentProject != null) {
            for (Phase phase : currentProject.getPhases()) {
                if (phase.getPhaseID() == task.getPhaseID()) {
                    List<Task> tasks = phase.getTasks();
                    if (tasks == null) {
                        tasks = new ArrayList<>();
                        phase.setTasks(tasks);
                    }
                    tasks.add(task);
                    ProjectHolder.set(currentProject);
                    result.setValue(task);
                    messageLiveData.setValue("Tạo task thành công");
                    Log.d(TAG, "Created task: " + task.getTaskName());
                    return result;
                }
            }
        }
        
        messageLiveData.setValue("Không tìm thấy phase để tạo task");
        Log.e(TAG, "Phase not found for task creation");
        return result;
    }

    public LiveData<Task> updateTask(Task task) {
        MutableLiveData<Task> result = new MutableLiveData<>();
        
        // Gọi API để cập nhật task
        TaskService.markTaskAsComplette(
            context,
            task.getTaskID(),
            task.getStatus(),
            response -> {
                if ("success".equals(response.optString("status"))) {
                    // Cập nhật task trong bộ nhớ local
                    Project currentProject = ProjectHolder.get();
                    if (currentProject != null) {
                        for (Phase phase : currentProject.getPhases()) {
                            if (phase.getPhaseID() == task.getPhaseID()) {
                                List<Task> tasksInPhase = phase.getTasks();
                                if (tasksInPhase != null) {
                                    for (int i = 0; i < tasksInPhase.size(); i++) {
                                        if (tasksInPhase.get(i).getTaskID() == task.getTaskID()) {
                                            tasksInPhase.set(i, task);
                                            ProjectHolder.set(currentProject);
                                            result.setValue(task);
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    String error = response.optString("error");
                    Log.e(TAG, "Error updating task: " + error);
                }
            },
            error -> {
                Log.e(TAG, "Error updating task", error);
            }
        );
        
        return result;
    }

    public void deleteTask(Task task) {
        Project currentProject = ProjectHolder.get();
        if (currentProject != null) {
            for (Phase phase : currentProject.getPhases()) {
                List<Task> tasksInPhase = phase.getTasks();
                if (tasksInPhase != null) {
                    if (tasksInPhase.removeIf(t -> t.getTaskID() == task.getTaskID())) {
                        ProjectHolder.set(currentProject); // Cập nhật ProjectHolder
                        return;
                    }
                }
            }
        }
    }

    // Phương thức mới để xử lý việc di chuyển task giữa các phase
    public void moveTaskBetweenPhases(Task taskToMove, Phase oldPhase, int oldOrderIndex, Phase newPhase, int newOrderIndex) {
        Project currentProject = ProjectHolder.get();
        if (currentProject != null) {
            // 1. Xóa task khỏi phase cũ
            if (oldPhase != null && oldPhase.getTasks() != null) {
                oldPhase.getTasks().remove(taskToMove);
            }

            // 2. Thêm task vào phase mới ở vị trí mới
            if (newPhase != null) {
                if (newPhase.getTasks() == null) {
                    newPhase.setTasks(new ArrayList<>());
                }
                // Cập nhật phaseID và orderIndex của task
                taskToMove.setPhaseID(newPhase.getPhaseID());
                taskToMove.setOrderIndex(newOrderIndex);
                newPhase.getTasks().add(newOrderIndex, taskToMove);
            }
            ProjectHolder.set(currentProject); // Cập nhật ProjectHolder sau khi di chuyển
        }
    }

    public LiveData<Task> fetchTaskDetail(int taskId) {
        TaskService.getTaskDetail(context, taskId,
            response -> {
                try {
                    if ("success".equals(response.optString("status"))) {
                        JSONObject data = response.getJSONObject("data");
                        Task task = new Task();
                        task.setTaskID(data.optInt("id"));
                        task.setTaskName(data.optString("taskName"));
                        task.setTaskDescription(data.optString("description"));
                        task.setPhaseID(data.optInt("phaseId"));
                        task.setAssignedTo(data.optInt("assignedToId"));
                        task.setStatus(data.optString("status"));
                        task.setPriority(data.optString("priority"));
                        
                        // Parse dates
                        String dueDateStr = data.optString("dueDate");
                        if (dueDateStr != null && !dueDateStr.isEmpty()) {
                            task.setDueDate(ParseDateUtil.parseDate(dueDateStr));
                        }
                        
                        task.setOrderIndex(data.optInt("orderIndex"));
                        currentTaskLiveData.setValue(task);
                        messageLiveData.setValue("Lấy thông tin task thành công");
                    } else {
                        String error = response.optString("error");
                        messageLiveData.setValue("Lỗi: " + error);
                        Log.e(TAG, "Error fetching task: " + error);
                    }
                } catch (JSONException e) {
                    messageLiveData.setValue("Lỗi xử lý dữ liệu");
                    Log.e(TAG, "Error parsing task data", e);
                }
            },
            error -> {
                messageLiveData.setValue("Lỗi kết nối");
                Log.e(TAG, "Error fetching task", error);
            }
        );
        return currentTaskLiveData;
    }

    public LiveData<Task> getCurrentTask() {
        return currentTaskLiveData;
    }

    public LiveData<List<User>> fetchProjectMembers() {
        int projectId = ProjectHolder.get() != null ? ProjectHolder.get().getProjectID() : -1;
        if (projectId == -1) {
            messageLiveData.setValue("Không tìm thấy projectId");
            return projectMembersLiveData;
        }

        TaskService.getProjectMembers(context, projectId,
            response -> {
                try {
                    if ("success".equals(response.optString("status"))) {
                        List<User> users = new ArrayList<>();
                        int totalMembers = response.getJSONArray("data").length();
                        final int[] processedMembers = {0};

                        for (int i = 0; i < totalMembers; i++) {
                            org.json.JSONObject obj = response.getJSONArray("data").getJSONObject(i);
                            int userId = obj.optInt("userId");

                            // Lấy thông tin chi tiết của user
                            TaskService.getUserInfo(context, userId,
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
                                            user.setGender(userData.optString("gender"));
                                            user.setBio(userData.optString("bio"));
                                            
                                            // Parse dates
                                            String birthdayStr = userData.optString("birthday");
                                            if (birthdayStr != null && !birthdayStr.isEmpty()) {
                                                user.setBirthday(ParseDateUtil.parseDate(birthdayStr));
                                            }
                                            
                                            String createdAtStr = userData.optString("created_at");
                                            if (createdAtStr != null && !createdAtStr.isEmpty()) {
                                                user.setCreated_at(ParseDateUtil.parseDate(createdAtStr));
                                            }
                                            
                                            user.setEmail_verified(userData.optBoolean("email_verified", false));
                                            
                                            users.add(user);
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error parsing user data", e);
                                    }

                                    // Kiểm tra nếu đã xử lý xong tất cả members
                                    processedMembers[0]++;
                                    if (processedMembers[0] == totalMembers) {
                                        projectMembersLiveData.setValue(users);
                                        messageLiveData.setValue("Lấy danh sách thành viên thành công");
                                    }
                                },
                                error -> {
                                    Log.e(TAG, "Error fetching user info", error);
                                    processedMembers[0]++;
                                    if (processedMembers[0] == totalMembers) {
                                        projectMembersLiveData.setValue(users);
                                        messageLiveData.setValue("Lấy danh sách thành viên thất bại");
                                    }
                                }
                            );
                        }
                    } else {
                        String error = response.optString("error");
                        messageLiveData.setValue("Lỗi: " + error);
                    }
                } catch (Exception e) {
                    messageLiveData.setValue("Lỗi khi phân tích dữ liệu thành viên: " + e.getMessage());
                }
            },
            error -> messageLiveData.setValue("Lỗi kết nối khi lấy thành viên dự án")
        );
        return projectMembersLiveData;
    }
} 