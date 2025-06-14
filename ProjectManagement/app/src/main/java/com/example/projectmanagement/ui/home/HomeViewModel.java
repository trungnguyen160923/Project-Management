package com.example.projectmanagement.ui.home;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.data.repository.ProjectRepository;

import java.util.List;

public class HomeViewModel extends ViewModel {
    private static final String TAG = "HomeViewModel";
    private final MutableLiveData<List<Project>> projects = new MutableLiveData<>();
    private ProjectRepository projectRepository;

    public void init(Context context) {
        Log.d(TAG, "Initializing HomeViewModel");
        projectRepository = ProjectRepository.getInstance(context);
        loadProjects();
    }

    public void refresh() {
        Log.d(TAG, "Refreshing projects");
        loadProjects();
    }

    private void loadProjects() {
        Log.d(TAG, "Loading projects");
        projectRepository.getProjects().observeForever(projectsList -> {
            Log.d(TAG, "Received projects: " + (projectsList != null ? projectsList.size() : 0));
            projects.setValue(projectsList);
        });
    }

    public LiveData<List<Project>> getProjects() {
        return projects;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "HomeViewModel cleared");
        if (projectRepository != null) {
            projectRepository.getProjects().removeObserver(projects::setValue);
        }
    }
}
