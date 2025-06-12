package com.example.projectmanagement.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.projectmanagement.data.service.TaskService;
import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.data.model.ProjectHolder;
import com.example.projectmanagement.data.model.Task;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

public class TaskRepository {
    private static final String TAG = "TaskRepository";
    private static TaskRepository instance;
    private final Context context;
    private final MutableLiveData<List<Task>> tasksLiveData;
    private final MutableLiveData<String> messageLiveData;

    private TaskRepository(Context ctx) {
        this.context = ctx.getApplicationContext();
        this.tasksLiveData = new MutableLiveData<>(new ArrayList<>());
        this.messageLiveData = new MutableLiveData<>();
    }

    public static TaskRepository getInstance(Context ctx) {
        if (instance == null) {
            instance = new TaskRepository(ctx);
        }
        return instance;
    }

    public LiveData<List<Task>> getPhaseTasks(int phaseId) {
        final int finalPhaseId = phaseId;
        TaskService.getPhaseTasks(context, phaseId, response -> {
            try {
                if ("success".equals(response.optString("status"))) {
                    List<Task> tasks = TaskService.parseTasksList(response);
                    tasksLiveData.setValue(tasks);
                    Log.d(TAG, "Loaded " + tasks.size() + " tasks for phase " + finalPhaseId);
                } else {
                    String err = response.optString("message", "Lấy tasks thất bại");
                    Log.e(TAG, err);
                    tasksLiveData.setValue(null);
                    messageLiveData.setValue(err);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Parsing error", e);
                tasksLiveData.setValue(null);
                messageLiveData.setValue("Lỗi phân tích dữ liệu server");
            }
        }, error -> {
            Log.e(TAG, "Network error", error);
            tasksLiveData.setValue(null);
            messageLiveData.setValue(error.getMessage());
        });

        return tasksLiveData;
    }

    public LiveData<String> getMessageLiveData() {
        return messageLiveData;
    }

    public LiveData<Task> createTask(Task task) {
        final MutableLiveData<Task> result = new MutableLiveData<>();
        final Task finalTask = task;
        
        TaskService.createTask(context, task, response -> {
            try {
                if ("success".equals(response.optString("status"))) {
                    Task createdTask = TaskService.parseTask(response);
                    List<Task> currentTasks = tasksLiveData.getValue();
                    if (currentTasks == null) {
                        currentTasks = new ArrayList<>();
                    }
                    currentTasks.add(createdTask);
                    tasksLiveData.setValue(currentTasks);
                    result.setValue(createdTask);
                    messageLiveData.setValue("Tạo task thành công");
                    Log.d(TAG, "Created task: " + finalTask.getTaskName());
                } else {
                    String err = response.optString("message", "Tạo task thất bại");
                    Log.e(TAG, err);
                    result.setValue(null);
                    messageLiveData.setValue(err);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Parsing error", e);
                result.setValue(null);
                messageLiveData.setValue("Lỗi phân tích dữ liệu server");
            }
        }, error -> {
            Log.e(TAG, "Network error", error);
            result.setValue(null);
            messageLiveData.setValue(error.getMessage());
        });

        return result;
    }

    public LiveData<Task> updateTask(Task task) {
        MutableLiveData<Task> result = new MutableLiveData<>();
        Project currentProject = ProjectHolder.get();
        if (currentProject != null) {
            for (Phase phase : currentProject.getPhases()) {
                if (phase.getPhaseID() == task.getPhaseID()) {
                    List<Task> tasksInPhase = phase.getTasks();
                    if (tasksInPhase != null) {
                        for (int i = 0; i < tasksInPhase.size(); i++) {
                            if (tasksInPhase.get(i).getTaskID() == task.getTaskID()) {
                                // Nếu task vẫn ở cùng một phase, chỉ cập nhật task tại vị trí đó
                                tasksInPhase.set(i, task);
                                ProjectHolder.set(currentProject); // Cập nhật ProjectHolder
                    result.setValue(task);
                                return result;
                }
            }
                    }
                }
            }
            // Nếu task được di chuyển sang phase khác, cần xóa khỏi phase cũ và thêm vào phase mới
            // Logic này sẽ được xử lý trong TaskActivity/TaskViewModel khi gọi updateTaskPhaseAndOrder
            // Tuy nhiên, nếu updateTask được gọi độc lập, chúng ta cần xử lý di chuyển ở đây.
            // Để đơn giản, giả định updateTaskPhaseAndOrder đã xử lý việc di chuyển.
            // Nếu task không tìm thấy trong phase hiện tại, có thể nó đã bị xóa hoặc di chuyển
            // Hoặc đây là một trường hợp đặc biệt không cần cập nhật toàn bộ project ở đây
            // Tôi sẽ chỉ cập nhật nếu task ở cùng phase
        }
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
} 