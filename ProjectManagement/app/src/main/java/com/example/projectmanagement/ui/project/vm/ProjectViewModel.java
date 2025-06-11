package com.example.projectmanagement.ui.project.vm;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.data.model.Task;
import com.example.projectmanagement.data.repository.ProjectRepository;

import java.util.ArrayList;
import java.util.List;

public class ProjectViewModel extends ViewModel {
    private ProjectRepository projectRepository;
    private final MutableLiveData<Project> project = new MutableLiveData<>();
    private final MutableLiveData<List<Phase>> phases = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isInputMode = new MutableLiveData<>(false);
    private int pendingPhase = -1;

    public void init(Context context) {
        projectRepository = ProjectRepository.getInstance(context);
        phases.setValue(new ArrayList<>());
    }

    public void setProject(Project project) {
        this.project.setValue(project);
        // TODO: Load phases from API
    }

    public LiveData<Project> getProject() {
        return project;
    }

    public LiveData<List<Phase>> getPhases() {
        return phases;
    }

    public LiveData<Boolean> getIsInputMode() {
        return isInputMode;
    }

    public void addPhase(int phaseId) {
        List<Phase> currentPhases = phases.getValue();
        if (currentPhases != null) {
            // TODO: Add phase to API
            Phase newPhase = new Phase();
            newPhase.setPhaseID(phaseId);
            newPhase.setPhaseName("Phase " + phaseId);
            currentPhases.add(newPhase);
            phases.setValue(currentPhases);
        }
    }

    public void deletePhase(int position) {
        List<Phase> currentPhases = phases.getValue();
        if (currentPhases != null && position >= 0 && position < currentPhases.size()) {
            // TODO: Delete phase from API
            currentPhases.remove(position);
            phases.setValue(currentPhases);
        }
    }

    public void toggleInputMode() {
        Boolean current = isInputMode.getValue();
        isInputMode.setValue(current != null && !current);
    }

    public void enterInputMode() {
        isInputMode.setValue(true);
    }

    public void exitInputMode() {
        isInputMode.setValue(false);
        pendingPhase = -1;
    }

    public void setPendingPhase(int phasePosition) {
        pendingPhase = phasePosition;
    }

    public int getPendingPhase() {
        return pendingPhase;
    }

    public void addTask(int phasePosition, String taskName) {
        List<Phase> currentPhases = phases.getValue();
        if (currentPhases != null && phasePosition >= 0 && phasePosition < currentPhases.size()) {
            Phase phase = currentPhases.get(phasePosition);
            Task newTask = new Task(taskName, "", "WORKING", "", null, null);
            phase.getTasks().add(newTask);
            phases.setValue(currentPhases);
        }
    }

    public void moveTask(Phase sourcePhase, Phase targetPhase, Task task, int dropIndex) {
        List<Phase> currentPhases = phases.getValue();
        if (currentPhases != null) {
            List<Task> sourceTasks = sourcePhase.getTasks();
            List<Task> targetTasks = targetPhase.getTasks();
            int originalIndex = sourceTasks.indexOf(task);

            if (sourceTasks.remove(task)) {
                if (sourcePhase == targetPhase && dropIndex > originalIndex) {
                    dropIndex--;
                }
                dropIndex = Math.max(0, Math.min(dropIndex, targetTasks.size()));
                targetTasks.add(dropIndex, task);
                phases.setValue(currentPhases);
            }
        }
    }

    public void updateProjectName(String name) {
        Project currentProject = project.getValue();
        if (currentProject != null) {
            currentProject.setProjectName(name);
            project.setValue(currentProject);
        }
    }

    public void updateProjectDescription(String description) {
        Project currentProject = project.getValue();
        if (currentProject != null) {
            currentProject.setProjectDescription(description);
            project.setValue(currentProject);
        }
    }

    public void loadProjectDetail(String projectId) {
        projectRepository.getProjectDetail(projectId).observeForever(projectData -> {
            if (projectData != null) {
                this.project.setValue(projectData);
                // TODO: Load phases from API
                phases.setValue(new ArrayList<>());
            }
        });
    }
} 