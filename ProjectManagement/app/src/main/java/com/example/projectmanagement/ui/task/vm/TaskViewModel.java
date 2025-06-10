package com.example.projectmanagement.ui.task.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.projectmanagement.data.model.Comment;
import com.example.projectmanagement.data.model.File;
import com.example.projectmanagement.data.model.Task;
import com.example.projectmanagement.data.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;

public class TaskViewModel extends ViewModel {
    private final TaskRepository taskRepository;
    private final MutableLiveData<Task> task = new MutableLiveData<>();
    private final MutableLiveData<List<Comment>> comments = new MutableLiveData<>();
    private final MutableLiveData<List<File>> files = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isImagesExpanded = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isFilesExpanded = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isCommentsExpanded = new MutableLiveData<>(false);

    public TaskViewModel() {
        taskRepository = TaskRepository.getInstance();
    }

    public void setTask(Task task) {
        this.task.setValue(task);
        loadComments();
        loadFiles();
    }

    private void loadComments() {
        Task currentTask = task.getValue();
        if (currentTask != null) {
            comments.setValue(currentTask.getComments());
        }
    }

    private void loadFiles() {
        Task currentTask = task.getValue();
        if (currentTask != null) {
            files.setValue(currentTask.getFiles());
        }
    }

    public LiveData<Task> getTask() {
        return task;
    }

    public LiveData<List<Comment>> getComments() {
        return comments;
    }

    public LiveData<List<File>> getFiles() {
        return files;
    }

    public LiveData<Boolean> getIsImagesExpanded() {
        return isImagesExpanded;
    }

    public LiveData<Boolean> getIsFilesExpanded() {
        return isFilesExpanded;
    }

    public LiveData<Boolean> getIsCommentsExpanded() {
        return isCommentsExpanded;
    }

    public void toggleImagesExpanded() {
        isImagesExpanded.setValue(!isImagesExpanded.getValue());
    }

    public void toggleFilesExpanded() {
        isFilesExpanded.setValue(!isFilesExpanded.getValue());
    }

    public void toggleCommentsExpanded() {
        isCommentsExpanded.setValue(!isCommentsExpanded.getValue());
    }

    public void addComment(String content) {
        Task currentTask = task.getValue();
        List<Comment> currentComments = comments.getValue();
        if (currentTask != null && currentComments != null) {
            Comment newComment = new Comment(
                currentComments.size() + 1,
                content,
                currentTask.getTaskID(),
                1, // TODO: Replace with actual user ID
                new java.util.Date()
            );
            currentComments.add(newComment);
            comments.setValue(currentComments);
        }
    }

    public void editComment(Comment comment, String newContent) {
        List<Comment> currentComments = comments.getValue();
        if (currentComments != null) {
            for (int i = 0; i < currentComments.size(); i++) {
                if (currentComments.get(i).getId() == comment.getId()) {
                    currentComments.get(i).setContent(newContent);
                    currentComments.get(i).setUpdateAt(new java.util.Date());
                    comments.setValue(currentComments);
                    break;
                }
            }
        }
    }

    public void deleteComment(Comment comment) {
        List<Comment> currentComments = comments.getValue();
        if (currentComments != null) {
            currentComments.removeIf(c -> c.getId() == comment.getId());
            comments.setValue(currentComments);
        }
    }

    public void addFile(File file) {
        List<File> currentFiles = files.getValue();
        if (currentFiles != null) {
            currentFiles.add(file);
            files.setValue(currentFiles);
        }
    }

    public void deleteFile(File file) {
        List<File> currentFiles = files.getValue();
        if (currentFiles != null) {
            currentFiles.removeIf(f -> f.getId() == file.getId());
            files.setValue(currentFiles);
        }
    }

    public void updateTaskStatus(String status) {
        Task currentTask = task.getValue();
        if (currentTask != null) {
            currentTask.setStatus(status);
            currentTask.setLastUpdate(new java.util.Date());
            task.setValue(currentTask);
        }
    }

    public void updateTaskDescription(String description) {
        Task currentTask = task.getValue();
        if (currentTask != null) {
            currentTask.setTaskDescription(description);
            currentTask.setLastUpdate(new java.util.Date());
            task.setValue(currentTask);
        }
    }
} 