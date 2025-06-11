package com.example.projectmanagement.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.data.model.ProjectHolder;
import com.example.projectmanagement.data.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskRepository {
    private static TaskRepository instance;
    private final MutableLiveData<List<Task>> tasksLiveData = new MutableLiveData<>();

    private TaskRepository() {
        tasksLiveData.setValue(new ArrayList<>());
    }

    public static synchronized TaskRepository getInstance() {
        if (instance == null) {
            instance = new TaskRepository();
        }
        return instance;
    }

    public LiveData<List<Task>> getAllTasks() {
        return tasksLiveData;
    }

    public LiveData<Task> createTask(Task task) {
        MutableLiveData<Task> result = new MutableLiveData<>();
        Project currentProject = ProjectHolder.get();
        if (currentProject != null) {
            for (Phase phase : currentProject.getPhases()) {
                if (phase.getPhaseID() == task.getPhaseID()) {
                    // Đảm bảo list tasks không null
                    if (phase.getTasks() == null) {
                        phase.setTasks(new ArrayList<>());
        }
                    phase.getTasks().add(task.getOrderIndex(), task);
                    ProjectHolder.set(currentProject); // Cập nhật ProjectHolder
                    break;
                }
            }
        }
        result.setValue(task);
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