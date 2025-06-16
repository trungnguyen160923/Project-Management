package com.example.projectmanagement.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Model Comment implementing Parcelable
 */
public class Comment implements Parcelable {
    private int id;
    private String content;
    private int taskID;
    private int userID;
    private Date createAt;
    private Date updateAt;
    private boolean isTaskResult;

    public Comment() { }

    public Comment(int id, String content, int taskID, int userID, Date createAt, boolean isTaskResult) {
        this.id = id;
        this.content = content;
        this.taskID = taskID;
        this.userID = userID;
        this.createAt = createAt;
        this.isTaskResult = isTaskResult;
    }

    public Comment(int id,
                   String content,
                   int taskID,
                   int userID,
                   Date createAt,
                   Date updateAt, boolean isTaskResult) {
        this.id = id;
        this.content = content;
        this.taskID = taskID;
        this.userID = userID;
        this.createAt = createAt;
        this.updateAt = updateAt;
        this.isTaskResult = isTaskResult;
    }

    protected Comment(Parcel in) {
        id        = in.readInt();
        content   = in.readString();
        taskID    = in.readInt();
        userID    = in.readInt();
        long ca   = in.readLong();
        createAt  = ca == -1 ? null : new Date(ca);
        long ua   = in.readLong();
        updateAt  = ua == -1 ? null : new Date(ua);
        isTaskResult = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(content);
        dest.writeInt(taskID);
        dest.writeInt(userID);
        dest.writeLong(createAt != null ? createAt.getTime() : -1);
        dest.writeLong(updateAt != null ? updateAt.getTime() : -1);
        dest.writeByte((byte) (isTaskResult ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getTaskID() { return taskID; }
    public void setTaskID(int taskID) { this.taskID = taskID; }

    public int getUserID() { return userID; }
    public void setUserID(int userID) { this.userID = userID; }

    public Date getCreateAt() { return createAt; }
    public void setCreateAt(Date createAt) { this.createAt = createAt; }

    public Date getUpdateAt() { return updateAt; }
    public void setUpdateAt(Date updateAt) { this.updateAt = updateAt; }

    public boolean isTaskResult() { return isTaskResult; }
    public void setIsTaskResult(boolean isTaskResult) { this.isTaskResult = isTaskResult; }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", taskID=" + taskID +
                ", userID=" + userID +
                ", createAt=" + createAt +
                ", updateAt=" + updateAt +
                ", isTaskResult=" + isTaskResult +
                '}';
    }
}
