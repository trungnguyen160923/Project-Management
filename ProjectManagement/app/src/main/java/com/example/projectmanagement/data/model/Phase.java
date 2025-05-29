package com.example.projectmanagement.data.model;

import java.util.Date;
import java.util.List;

public class Phase {
    private int phaseID;
    private int projectID;
    private String phaseName;
    private String description;
    private int orderIndex;
    private Date createAt;


    private List<Task> tasks;

    public Phase() {
    }

    public Phase(int phaseID, int projectID, String phaseName, String description,
                 int orderIndex, Date createAt) {
        this.phaseID = phaseID;
        this.projectID = projectID;
        this.phaseName = phaseName;
        this.description = description;
        this.orderIndex = orderIndex;
        this.createAt = createAt;
    }

    public Phase(String phaseName, List<Task> tasks) {
        this.phaseName = phaseName;
        this.tasks = tasks;
    }

    public int getPhaseID() {
        return phaseID;
    }

    public void setPhaseID(int phaseID) {
        this.phaseID = phaseID;
    }

    public int getProjectID() {
        return projectID;
    }

    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }

    public String getPhaseName() {
        return phaseName;
    }

    public void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        orderIndex = orderIndex;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
}
