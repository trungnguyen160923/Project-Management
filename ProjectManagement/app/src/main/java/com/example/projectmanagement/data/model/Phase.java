package com.example.projectmanagement.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Model Phase implementing Parcelable
 */
public class Phase implements Parcelable {
    private int phaseID;
    private int projectID;
    private String phaseName;
    private String description;
    private int orderIndex;
    private Date createAt;
    private List<Task> tasks;

    public Phase() {
        this.tasks = new ArrayList<>();
    }

    public Phase(String phaseName, List<Task> tasks) {
        this.phaseName = phaseName;
        this.tasks = tasks;
    }

    public Phase(int phaseID,
                 int projectID,
                 String phaseName,
                 String description,
                 int orderIndex,
                 Date createAt,
                 List<Task> tasks) {
        this.phaseID = phaseID;
        this.projectID = projectID;
        this.phaseName = phaseName;
        this.description = description;
        this.orderIndex = orderIndex;
        this.createAt = createAt;
        this.tasks = tasks != null ? tasks : new ArrayList<>();
    }

    // Parcelable constructor
    protected Phase(Parcel in) {
        phaseID      = in.readInt();
        projectID    = in.readInt();
        phaseName    = in.readString();
        description  = in.readString();
        orderIndex   = in.readInt();
        long createTs = in.readLong();
        createAt     = createTs == -1 ? null : new Date(createTs);
        // Read tasks list (Task must implement Parcelable)
        tasks        = in.createTypedArrayList(Task.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(phaseID);
        dest.writeInt(projectID);
        dest.writeString(phaseName);
        dest.writeString(description);
        dest.writeInt(orderIndex);
        dest.writeLong(createAt != null ? createAt.getTime() : -1);
        // Write tasks list
        dest.writeTypedList(tasks);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Phase> CREATOR = new Creator<Phase>() {
        @Override
        public Phase createFromParcel(Parcel in) {
            return new Phase(in);
        }

        @Override
        public Phase[] newArray(int size) {
            return new Phase[size];
        }
    };

    // ===== Getters & Setters =====

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
        this.orderIndex = orderIndex;
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
