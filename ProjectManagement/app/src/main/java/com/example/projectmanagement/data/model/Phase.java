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


    private List<Task> task;

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

    public Phase(String phaseName, List<Task> task) {
        this.phaseName = phaseName;
        this.task = task;
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

    public List<Task> getTask() {
        return task;
    }

    public void setTask(List<Task> task) {
        this.task = task;
    }
}
