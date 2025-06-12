package com.example.projectmanagement.ui.project.vm;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.data.model.ProjectHolder;
import com.example.projectmanagement.data.model.Task;
import com.example.projectmanagement.data.repository.ProjectRepository;
import com.example.projectmanagement.data.repository.PhaseRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProjectViewModel extends ViewModel {
    private ProjectRepository projectRepository;
    private PhaseRepository phaseRepository;
    private final MutableLiveData<Project> project = new MutableLiveData<>();
    private final MutableLiveData<List<Phase>> phases = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isInputMode = new MutableLiveData<>(false);
    private int pendingPhase = -1;
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public void init(Context context) {
        projectRepository = ProjectRepository.getInstance(context);
        phaseRepository = PhaseRepository.getInstance(context);
        phases.setValue(new ArrayList<>());
    }

    public void setProject(Project project) {
        Log.d("ProjectViewModel", "Setting project: " + project.getProjectName() + 
            ", phases: " + (project.getPhases() != null ? project.getPhases().size() : 0));
        
        // Set project first
        this.project.setValue(project);
        
        // Set phases immediately if project has them
        if (project.getPhases() != null && !project.getPhases().isEmpty()) {
            Log.d("ProjectViewModel", "Using existing phases from project: " + project.getPhases().size());
            phases.setValue(project.getPhases());
        } else {
            // Load phases from API
            Log.d("ProjectViewModel", "Loading phases from API for project: " + project.getProjectID());
            loadProjectPhases(project.getProjectID());
        }
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
        // Get current phases list
        List<Phase> currentPhases = phases.getValue();
        if (currentPhases == null) {
            currentPhases = new ArrayList<>();
        }

        // Create new phase with next order index
        Phase newPhase = new Phase();
        newPhase.setPhaseName("Phase " + (currentPhases.size() + 1));
        newPhase.setDescription("Description for " + newPhase.getPhaseName());
        newPhase.setStatus("ACTIVE");
        newPhase.setProjectID(project.getValue().getProjectID());
        newPhase.setOrderIndex(currentPhases.size()); // Set order index to last position

        // Call repository to create phase
        phaseRepository.createPhase(newPhase)
                .observeForever(createdPhase -> {
                    if (createdPhase != null) {
                        List<Phase> tmpCurrentPhases = phases.getValue() != null
                                ? new ArrayList<>(phases.getValue())
                                : new ArrayList<>();
                        tmpCurrentPhases.add(createdPhase);
                        phases.setValue(tmpCurrentPhases);
                        Log.d("ProjectViewModel",
                                "Phase added successfully: " + createdPhase.getPhaseName() +
                                        " with order index: " + createdPhase.getOrderIndex());
                    }
                });


        // Observe messages
        phaseRepository.getMessageLiveData().observeForever(msg -> {
            if (msg != null) {
                message.setValue(msg);
                Log.d("ProjectViewModel", "Phase creation message: " + msg);
            }
        });
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

    public void loadProjectDetail(int projectId) {
        projectRepository.getProject(projectId).observeForever(projectData -> {
            if (projectData != null) {
                this.project.setValue(projectData);
                // Load phases
                loadProjectPhases(projectId);
            }
        });
    }

    private void loadProjectPhases(int projectId) {
        Log.d("ProjectViewModel", "Loading phases for project: " + projectId);
        phaseRepository.getProjectPhases(projectId).observeForever(phasesList -> {
            if (phasesList != null && !phasesList.isEmpty()) {
                Log.d("ProjectViewModel", "Loaded " + phasesList.size() + " phases from API");
                this.phases.setValue(phasesList);
                
                // Update project with loaded phases
                Project currentProject = project.getValue();
                if (currentProject != null) {
                    currentProject.setPhases(phasesList);
                    project.setValue(currentProject);
                }
                
                // Load tasks for each phase
                for (Phase phase : phasesList) {
                    loadPhaseTasks(phase.getPhaseID());
                }
            } else {
                Log.e("ProjectViewModel", "No phases loaded from API");
                message.setValue("No phases found");
            }
        });
    }

    private void loadPhaseTasks(int phaseId) {
        Log.d("ProjectViewModel", "Loading tasks for phase: " + phaseId);
        
        // Get project from ProjectHolder
        Project currentProject = ProjectHolder.get();
        if (currentProject == null || currentProject.getPhases() == null) {
            Log.e("ProjectViewModel", "Project or phases not found in ProjectHolder");
            return;
        }

        // Find phase and its tasks
        Phase targetPhase = null;
        for (Phase phase : currentProject.getPhases()) {
            if (phase.getPhaseID() == phaseId) {
                targetPhase = phase;
                break;
            }
        }

        if (targetPhase == null) {
            Log.e("ProjectViewModel", "Phase not found: " + phaseId);
            return;
        }

        // Get tasks from phase
        List<Task> tasks = targetPhase.getTasks();
        Log.d("ProjectViewModel", "Found " + (tasks != null ? tasks.size() : 0) + " tasks for phase: " + phaseId);

        // Update phase with tasks
        List<Phase> currentPhases = phases.getValue();
        if (currentPhases != null) {
            for (Phase phase : currentPhases) {
                if (phase.getPhaseID() == phaseId) {
                    phase.setTasks(tasks);
                    phases.setValue(currentPhases);
                    Log.d("ProjectViewModel", "Updated phase " + phaseId + " with " + 
                        (tasks != null ? tasks.size() : 0) + " tasks");
                    break;
                }
            }
        }
    }

    public void createPhase(Phase phase) {
        phaseRepository.createPhase(phase).observeForever(createdPhase -> {
            if (createdPhase != null) {
                List<Phase> currentPhases = phases.getValue();
                if (currentPhases != null) {
                    currentPhases.add(createdPhase);
                    phases.setValue(currentPhases);
                }
            }
        });

        phaseRepository.getMessageLiveData().observeForever(msg -> {
            if (msg != null) {
                message.setValue(msg);
            }
        });
    }

    public LiveData<String> getMessage() {
        return message;
    }
} 