package com.example.projectmanagement.ui.home;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.data.repository.ProjectRepository;

import java.util.List;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<List<Project>> projects = new MutableLiveData<>();
    private ProjectRepository projectRepository;

    public void init(Context context) {
        projectRepository = ProjectRepository.getInstance(context);
        projectRepository.getAllProjects().observeForever(projectsList -> {
            projects.setValue(projectsList);
        });
    }

    public LiveData<List<Project>> getProjects() {
        return projects;
    }
}
