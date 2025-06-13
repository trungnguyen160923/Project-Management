package com.example.projectmanagement.ui.project.vm;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.data.repository.ProjectRepository;

/**
 * ViewModel for updating a project
 */
public class UpdateProjectViewModel extends ViewModel {
    private final MutableLiveData<UpdateProjectFormState> formState = new MutableLiveData<>(
            new UpdateProjectFormState(false, null)
    );
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Project> requestProject = new MutableLiveData<>();

    private LiveData<Project> updatedProject;
    private LiveData<String> serverMessage;
    private ProjectRepository repo;

    /**
     * Initialize repository and LiveData transformations
     */
    public void init(@NonNull Context context) {
        repo = ProjectRepository.getInstance(context.getApplicationContext());
        updatedProject = Transformations.switchMap(requestProject, project -> {
            isLoading.setValue(true);
            return repo.updateProject(project);
        });
        serverMessage = Transformations.switchMap(requestProject, project -> repo.getMessage());
    }

    public LiveData<UpdateProjectFormState> getFormState() {
        return formState;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getServerMessage() {
        return serverMessage;
    }

    /**
     * Called by UI to trigger update; returns LiveData<Boolean> indicating success
     */
    public LiveData<Boolean> updateProject(@NonNull Project project) {
        requestProject.setValue(project);
        return Transformations.map(updatedProject, result -> {
            isLoading.setValue(false);
            return result != null;
        });
    }

    /**
     * Validate project name field
     */
    public void dataChanged(@NonNull String name) {
        boolean valid = !name.trim().isEmpty();
        Integer err = valid ? null : R.string.empty_projectName;
        formState.setValue(new UpdateProjectFormState(valid, err));
    }

    /**
     * Form state for update button enabling
     */
    public static class UpdateProjectFormState {
        private final boolean isValid;
        private final Integer nameErrorRes;

        public UpdateProjectFormState(boolean isValid, Integer nameErrorRes) {
            this.isValid = isValid;
            this.nameErrorRes = nameErrorRes;
        }

        public boolean isValid() {
            return isValid;
        }

        public Integer getNameErrorRes() {
            return nameErrorRes;
        }
    }
}
