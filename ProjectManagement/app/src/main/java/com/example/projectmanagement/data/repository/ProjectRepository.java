package com.example.projectmanagement.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.projectmanagement.api.ApiClient;
import com.example.projectmanagement.api.ProjectService;
import com.example.projectmanagement.data.model.Project;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton repository managing projects.
 */
public class ProjectRepository {

    private final MutableLiveData<List<Project>> projectsLiveData = new MutableLiveData<>();
    private static ProjectRepository instance;
    private final ProjectService apiService;

    private ProjectRepository() {
        apiService = ApiClient.getClient().create(ProjectService.class);
        projectsLiveData.setValue(new ArrayList<>());
    }

    public static synchronized ProjectRepository getInstance() {
        if (instance == null) {
            instance = new ProjectRepository();
        }
        return instance;
    }

    /**
     * Returns live list of all projects.
     */
    public LiveData<List<Project>> getAllProjects() {
        return projectsLiveData;
    }

    /**
     * Creates a project locally (or via API) and returns LiveData of the created project.
     */
    public LiveData<Project> createProject(Project project) {
        MutableLiveData<Project> result = new MutableLiveData<>();

        List<Project> current = projectsLiveData.getValue();
        if (current == null) {
            current = new ArrayList<>();
        }
        current.add(project);
        projectsLiveData.setValue(current);
        result.setValue(project);
        return result;
    }
}
