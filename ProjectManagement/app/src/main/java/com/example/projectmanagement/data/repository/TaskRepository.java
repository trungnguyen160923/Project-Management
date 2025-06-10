package com.example.projectmanagement.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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
        List<Task> current = tasksLiveData.getValue();
        if (current == null) {
            current = new ArrayList<>();
        }
        current.add(task);
        tasksLiveData.setValue(current);
        result.setValue(task);
        return result;
    }

    public LiveData<Task> updateTask(Task task) {
        MutableLiveData<Task> result = new MutableLiveData<>();
        List<Task> current = tasksLiveData.getValue();
        if (current != null) {
            for (int i = 0; i < current.size(); i++) {
                if (current.get(i).getTaskID() == task.getTaskID()) {
                    current.set(i, task);
                    tasksLiveData.setValue(current);
                    result.setValue(task);
                    break;
                }
            }
        }
        return result;
    }

    public void deleteTask(Task task) {
        List<Task> current = tasksLiveData.getValue();
        if (current != null) {
            current.removeIf(t -> t.getTaskID() == task.getTaskID());
            tasksLiveData.setValue(current);
        }
    }
} 