//package com.example.projectmanagement.ui.task;
//
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.ViewModel;
//
//import com.example.projectmanagement.data.model.Task;
//import com.example.projectmanagement.data.repository.TaskRepository;
//
//import java.util.List;
//
//public class TaskViewModel extends ViewModel {
//    private final TaskRepository taskRepository;
//    private final MutableLiveData<Task> selectedTask = new MutableLiveData<>();
//
//    public TaskViewModel() {
//        taskRepository = TaskRepository.getInstance();
//    }
//
//    public LiveData<List<Task>> getAllTasks() {
//        return taskRepository.getAllTasks();
//    }
//
//    public LiveData<Task> createTask(Task task) {
//        return taskRepository.createTask(task);
//    }
//
//    public LiveData<Task> updateTask(Task task) {
//        return taskRepository.updateTask(task);
//    }
//
//    public void deleteTask(Task task) {
//        taskRepository.deleteTask(task);
//    }
//
//    public void selectTask(Task task) {
//        selectedTask.setValue(task);
//    }
//
//    public LiveData<Task> getSelectedTask() {
//        return selectedTask;
//    }
//}