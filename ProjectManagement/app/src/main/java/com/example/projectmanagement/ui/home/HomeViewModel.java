package com.example.projectmanagement.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.data.repository.ProjectRepository;

import java.util.List;

public class HomeViewModel extends ViewModel {
    private final LiveData<List<Project>> projects;

    public HomeViewModel() {
        projects = ProjectRepository.getInstance().getAllProjects();
    }

    public LiveData<List<Project>> getProjects() {
        return projects;
    }
}
