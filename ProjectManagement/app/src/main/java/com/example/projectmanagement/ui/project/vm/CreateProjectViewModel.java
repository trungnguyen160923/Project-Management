package com.example.projectmanagement.ui.project.vm;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.data.repository.ProjectRepository;

public class CreateProjectViewModel extends ViewModel {
    private final MutableLiveData<CreateProjectFormState> formState = new MutableLiveData<>(
            new CreateProjectFormState(false, null)
    );
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Project> requestProject = new MutableLiveData<>();

    private LiveData<Project> createdProject;
    private LiveData<String>  serverMessage;

    private ProjectRepository repo;

    public void init(@NonNull Context context) {
        repo = ProjectRepository.getInstance(context.getApplicationContext());
        // Khi requestProject thay đổi, switchMap tự xử lý subscribe/unsubscribe
        createdProject = Transformations.switchMap(
                requestProject,
                project -> {
                    isLoading.setValue(true);
                    return repo.createProject(project);
                }
        );
        serverMessage = Transformations.switchMap(
                requestProject,
                project -> repo.getMessage()
        );
    }

    public LiveData<CreateProjectFormState> getFormState() { return formState; }
    public LiveData<Boolean>               getIsLoading() { return isLoading; }
    public LiveData<String>                getServerMessage() { return serverMessage; }

    /**
     * Kick off tạo project và trả về LiveData<Boolean> báo thành công/thất bại.
     */
    public LiveData<Boolean> createProject(@NonNull Project project) {
        requestProject.setValue(project);
        return Transformations.map(
                createdProject,
                result -> {
                    isLoading.setValue(false);
                    return result != null;
                }
        );
    }

    public void dataChanged(@NonNull String name) {
        boolean valid = !name.trim().isEmpty();
        Integer err = valid ? null : R.string.empty_projectName;
        formState.setValue(new CreateProjectFormState(valid, err));
    }

    public static class CreateProjectFormState {
        private final boolean isValid;
        private final Integer nameErrorRes;
        public CreateProjectFormState(boolean isValid, Integer nameErrorRes) {
            this.isValid = isValid;
            this.nameErrorRes = nameErrorRes;
        }
        public boolean isValid() { return isValid; }
        public Integer getNameErrorRes() { return nameErrorRes; }
    }
}