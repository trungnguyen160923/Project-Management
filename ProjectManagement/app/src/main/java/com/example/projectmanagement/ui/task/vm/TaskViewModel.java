package com.example.projectmanagement.ui.task.vm;

import android.app.Application;
import android.net.Uri;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.projectmanagement.data.model.Comment;
import com.example.projectmanagement.data.model.File;
import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.data.model.ProjectHolder;
import com.example.projectmanagement.data.model.Task;
import com.example.projectmanagement.data.model.User;
import com.example.projectmanagement.data.repository.TaskRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskViewModel extends AndroidViewModel {
    private final TaskRepository taskRepository;
    private final MutableLiveData<Task> task = new MutableLiveData<>();
    private final MutableLiveData<List<Comment>> comments = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<File>> files = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Uri>> imageUris = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isImagesExpanded = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isFilesExpanded = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isCommentsExpanded = new MutableLiveData<>(false);
    private final MutableLiveData<String> taskDescription = new MutableLiveData<>("");
    private final MutableLiveData<List<Phase>> allProjectPhases = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<List<User>> projectMembers = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> imageUrls = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<String>> getImageUrls() {
        return imageUrls;
    }

    public void addImageUrl(String url) {
        List<String> current = imageUrls.getValue();
        current.add(url);
        imageUrls.setValue(current);
    }

    public TaskViewModel(Application application) {
        super(application);
        taskRepository = TaskRepository.getInstance(application);
        fetchPhases();
        fetchProjectMembers();
    }

    public void fetchProjectMembers() {
        taskRepository.fetchProjectMembers().observeForever(members -> {
            if (members != null) {
                projectMembers.setValue(members);
            }
        });
    }

    public LiveData<List<User>> getProjectMembers() {
        return projectMembers;
    }

    public User getMemberById(int userId) {
        List<User> members = projectMembers.getValue();
        if (members != null) {
            for (User member : members) {
                if (member.getId() == userId) {
                    return member;
                }
            }
        }
        return null;
    }

    public void assignTaskToMember(int taskId, int userId) {
        Task currentTask = task.getValue();
        if (currentTask != null && currentTask.getTaskID() == taskId) {
            currentTask.setAssignedTo(userId);
            currentTask.setLastUpdate(new Date());
            task.setValue(currentTask);
            taskRepository.updateTask(currentTask);
        }
    }

    public void setTask(Task task) {
        this.task.setValue(task);
        if (task != null) {
            taskDescription.setValue(task.getTaskDescription());
            loadComments();
            loadFiles();
        }
    }

    private void loadComments() {
        Task currentTask = task.getValue();
        if (currentTask != null) {
            List<Comment> taskComments = currentTask.getComments();
            if (taskComments != null) {
                comments.setValue(taskComments);
            }
        }
    }

    private void loadFiles() {
        Task currentTask = task.getValue();
        if (currentTask != null) {
            List<File> taskFiles = currentTask.getFiles();
            if (taskFiles != null) {
                files.setValue(taskFiles);
            }
        }
    }

    private void fetchPhases() {
        if (ProjectHolder.get() != null && ProjectHolder.get().getPhases() != null) {
            allProjectPhases.setValue(ProjectHolder.get().getPhases());
        }
    }

    // Getters for LiveData
    public LiveData<Task> getTask() {
        return task;
    }

    public LiveData<List<Comment>> getComments() {
        return comments;
    }

    public LiveData<List<File>> getFiles() {
        return files;
    }

    public LiveData<List<Uri>> getImageUris() {
        return imageUris;
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

    public LiveData<String> getTaskDescription() {
        return taskDescription;
    }

    public LiveData<List<Phase>> getAllProjectPhases() {
        return allProjectPhases;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    // Update methods for LiveData
    public void updateComments(List<Comment> newComments) {
        comments.setValue(newComments);
        Task currentTask = task.getValue();
        if (currentTask != null) {
            currentTask.setComments(newComments);
            task.setValue(currentTask);
            taskRepository.updateTask(currentTask);
        }
    }

    public void updateFiles(List<File> newFiles) {
        files.setValue(newFiles);
        Task currentTask = task.getValue();
        if (currentTask != null) {
            currentTask.setFiles(newFiles);
            task.setValue(currentTask);
            taskRepository.updateTask(currentTask);
        }
    }

    public void updateImageUris(List<Uri> newUris) {
        imageUris.postValue(newUris);
    }

    // Toggle methods for sections
    public void toggleImagesExpanded() {
        isImagesExpanded.setValue(!isImagesExpanded.getValue());
    }

    public void toggleFilesExpanded() {
        isFilesExpanded.setValue(!isFilesExpanded.getValue());
    }

    public void toggleCommentsExpanded() {
        isCommentsExpanded.setValue(!isCommentsExpanded.getValue());
    }

    // Comment operations
    public void addComment(String content) {
        List<Comment> currentComments = comments.getValue();
        Task currentTask = task.getValue();
        if (currentTask != null && currentComments != null) {
            Comment newComment = new Comment(
                currentComments.size() + 1,
                content,
                currentTask.getTaskID(),
                1, // TODO: Replace with actual user ID
                new Date()
            );
            currentComments.add(newComment);
            updateComments(currentComments);
        }
    }

    public void editComment(Comment comment, String newContent) {
        List<Comment> currentComments = comments.getValue();
        if (currentComments != null) {
            for (int i = 0; i < currentComments.size(); i++) {
                if (currentComments.get(i).getId() == comment.getId()) {
                    currentComments.get(i).setContent(newContent);
                    currentComments.get(i).setUpdateAt(new Date());
                    updateComments(currentComments);
                    break;
                }
            }
        }
    }

    public void deleteComment(Comment comment) {
        List<Comment> currentComments = comments.getValue();
        if (currentComments != null) {
            currentComments.removeIf(c -> c.getId() == comment.getId());
            updateComments(currentComments);
        }
    }

    // File operations
    public void addFile(File file) {
        List<File> currentFiles = files.getValue();
        if (currentFiles != null) {
            currentFiles.add(file);
            updateFiles(currentFiles);
        }
    }

    public void deleteFile(File file) {
        List<File> currentFiles = files.getValue();
        if (currentFiles != null) {
            currentFiles.removeIf(f -> f.getId() == file.getId());
            updateFiles(currentFiles);
        }
    }

    // Image operations
    public void addImageUri(Uri uri) {
        List<Uri> currentUris = imageUris.getValue();
        if (currentUris != null) {
            currentUris.add(uri);
            updateImageUris(currentUris);
        }
    }

    public void removeImageUri(int position) {
        List<Uri> currentUris = imageUris.getValue();
        if (currentUris != null && position >= 0 && position < currentUris.size()) {
            currentUris.remove(position);
            updateImageUris(currentUris);
        }
    }

    // Task operations
    public void markTaskAsComplete(boolean isCompleted) {
        Task currentTask = task.getValue();
        if (currentTask != null) {
            currentTask.setStatus(isCompleted ? "DONE" : "WORKING");
            currentTask.setLastUpdate(new Date());
            task.setValue(currentTask);
            
            // Cập nhật task trong ProjectHolder
            Project currentProject = ProjectHolder.get();
            if (currentProject != null && currentProject.getPhases() != null) {
                for (Phase phase : currentProject.getPhases()) {
                    if (phase.getTasks() != null) {
                        for (Task t : phase.getTasks()) {
                            if (t.getTaskID() == currentTask.getTaskID()) {
                                t.setStatus(currentTask.getStatus());
                                t.setLastUpdate(currentTask.getLastUpdate());
                                break;
                            }
                        }
                    }
                }
                ProjectHolder.set(currentProject);
            }
            
            taskRepository.updateTask(currentTask);
        }
    }

    public void updateTaskStatus(String status) {
        Task currentTask = task.getValue();
        if (currentTask != null) {
            currentTask.setStatus(status);
            currentTask.setLastUpdate(new Date());
            task.setValue(currentTask);
            taskRepository.updateTask(currentTask);
        }
    }

    public void updateTaskDescription(String description) {
        taskDescription.setValue(description);
        Task currentTask = task.getValue();
        if (currentTask != null) {
            currentTask.setTaskDescription(description);
            currentTask.setLastUpdate(new Date());
            task.setValue(currentTask);
            taskRepository.updateTask(currentTask);
        }
    }

    public void updateTaskPhaseAndOrder(int newPhaseId, int newOrderIndex) {
        Task currentTask = task.getValue();
        if (currentTask != null) {
            currentTask.setPhaseID(newPhaseId);
            currentTask.setOrderIndex(newOrderIndex);
            currentTask.setLastUpdate(new Date());
            task.setValue(currentTask);
            taskRepository.updateTask(currentTask);
        }
    }

    public void updateTaskDueDate(Date dueDate) {
        Task currentTask = task.getValue();
        if (currentTask != null) {
            currentTask.setDueDate(dueDate);
            currentTask.setLastUpdate(new Date());
            task.setValue(currentTask);
            taskRepository.updateTask(currentTask);
        }
    }

    public void fetchTaskDetail(int taskId) {
        taskRepository.fetchTaskDetail(taskId).observeForever(task -> {
            if (task != null) {
                setTask(task);
                // Load additional data
                loadComments();
                loadFiles();
                // Update UI state
                isImagesExpanded.setValue(false);
                isFilesExpanded.setValue(false);
                isCommentsExpanded.setValue(false);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up any resources if needed
    }

    public void updateTask(Task t) {
        task.setValue(t);
    }
} 