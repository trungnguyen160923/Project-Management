package com.example.projectmanagement.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.data.repository.ProjectRepository;

public class CreateProjectViewModel extends ViewModel {

    private final ProjectRepository projectRepository;
    private final MutableLiveData<Project> projectLiveData = new MutableLiveData<>();
    private final MutableLiveData<CreateProjectFormState> createFormState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public CreateProjectViewModel() {
        projectRepository = ProjectRepository.getInstance();
        createFormState.setValue(new CreateProjectFormState(false, null));
    }

    public LiveData<CreateProjectFormState> getCreateFormState() {
        return createFormState;
    }

    public LiveData<Project> getProjectLiveData() {
        return projectLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void createProject(Project project) {
        isLoading.setValue(true);
        projectRepository.createProject(project)
                .observeForever(created -> {
                    projectLiveData.setValue(created);
                    isLoading.setValue(false);
                });
    }

    public void dataChanged(String projectName) {
        String v = projectName != null ? projectName.trim() : "";
        if (v.isEmpty()) {
            createFormState.setValue(new CreateProjectFormState(false, R.string.empty_projectName));
        } else {
            createFormState.setValue(new CreateProjectFormState(true, null));
        }
    }

    /**
     * Form state for project creation.
     */
    public static class CreateProjectFormState {
        private final boolean isValid;
        private final Integer nameErrorRes;

        public CreateProjectFormState(boolean isValid, Integer nameErrorRes) {
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