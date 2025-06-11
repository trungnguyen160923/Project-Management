package com.example.projectmanagement.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.projectmanagement.utils.ParseDateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Model Task implementing Parcelable, with comments and files lists
 */
public class Task implements Parcelable {
    private int taskID;
    private int phaseID;
    private String taskName;
    private String taskDescription;
    private int assignedTo;
    private String status;
    private String priority;
    private Date dueDate;x
    private Boolean allowSelfAssign;
    private int orderIndex;
    private Date createAt;
    private Date lastUpdate;
    private List<Comment> comments;
    private List<File> files;

    public Task() {
        comments = new ArrayList<>();
        files = new ArrayList<>();
    }

    // Constructor for simple creation
    public Task(String taskName,
                String taskDescription,
                String status,
                String dueDate,
                List<Comment> comments,
                List<File> files) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.status = status;
        this.dueDate = ParseDateUtil.parseDate(dueDate);
        this.comments = comments != null ? comments : new ArrayList<>();
        this.files = files != null ? files : new ArrayList<>();
    }

    // Full constructor
    public Task(int taskID,
                int phaseID,
                String taskName,
                String taskDescription,
                int assignedTo,
                String status,
                String priority,
                Date dueDate,
                Boolean allowSelfAssign,
                int orderIndex,
                Date createAt,
                Date lastUpdate,
                List<Comment> comments,
                List<File> files) {
        this.taskID = taskID;
        this.phaseID = phaseID;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.assignedTo = assignedTo;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.allowSelfAssign = allowSelfAssign;
        this.orderIndex = orderIndex;
        this.createAt = createAt;
        this.lastUpdate = lastUpdate;
        this.comments = comments != null ? comments : new ArrayList<>();
        this.files = files != null ? files : new ArrayList<>();
    }

    // Parcelable constructor
    protected Task(Parcel in) {
        taskID          = in.readInt();
        phaseID         = in.readInt();
        taskName        = in.readString();
        taskDescription = in.readString();
        assignedTo      = in.readInt();
        status          = in.readString();
        priority        = in.readString();
        long dd         = in.readLong();
        dueDate         = dd == -1 ? null : new Date(dd);
        byte flag       = in.readByte();
        allowSelfAssign = flag == 0 ? null : (flag == 1);
        orderIndex      = in.readInt();
        long ca         = in.readLong();
        createAt        = ca == -1 ? null : new Date(ca);
        long lu         = in.readLong();
        lastUpdate      = lu == -1 ? null : new Date(lu);
        comments        = in.createTypedArrayList(Comment.CREATOR);
        files           = in.createTypedArrayList(File.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(taskID);
        dest.writeInt(phaseID);
        dest.writeString(taskName);
        dest.writeString(taskDescription);
        dest.writeInt(assignedTo);
        dest.writeString(status);
        dest.writeString(priority);
        dest.writeLong(dueDate != null ? dueDate.getTime() : -1);
        dest.writeByte(allowSelfAssign == null ? (byte)0 : (byte)(allowSelfAssign ? 1 : 2));
        dest.writeInt(orderIndex);
        dest.writeLong(createAt != null ? createAt.getTime() : -1);
        dest.writeLong(lastUpdate != null ? lastUpdate.getTime() : -1);
        dest.writeTypedList(comments);
        dest.writeTypedList(files);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    // ===== Getters & Setters =====

    public int getTaskID() { return taskID; }
    public void setTaskID(int taskID) { this.taskID = taskID; }

    public int getPhaseID() { return phaseID; }
    public void setPhaseID(int phaseID) { this.phaseID = phaseID; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public String getTaskDescription() { return taskDescription; }
    public void setTaskDescription(String taskDescription) { this.taskDescription = taskDescription; }

    public int getAssignedTo() { return assignedTo; }
    public void setAssignedTo(int assignedTo) { this.assignedTo = assignedTo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public Boolean getAllowSelfAssign() { return allowSelfAssign; }
    public void setAllowSelfAssign(Boolean allowSelfAssign) { this.allowSelfAssign = allowSelfAssign; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

    public Date getCreateAt() { return createAt; }
    public void setCreateAt(Date createAt) { this.createAt = createAt; }

    public Date getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(Date lastUpdate) { this.lastUpdate = lastUpdate; }

    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }

    public List<File> getFiles() { return files; }
    public void setFiles(List<File> files) { this.files = files; }
}
