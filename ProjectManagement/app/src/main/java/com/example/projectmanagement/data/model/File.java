package com.example.projectmanagement.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Model File implementing Parcelable
 */
public class File implements Parcelable {
    private int id;
    private String fileName;
    private String filePath;
    private int fileSize;
    private String fileType;
    private int taskID;
    private int userID;
    private Date createdAt;
    private Date updateAt;

    public File() {
    }

    public File(int id, String fileName, String filePath, int fileSize, String fileType, int taskID, int userID) {
        this.id = id;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.taskID = taskID;
        this.userID = userID;
    }

    public File(int id,
                String fileName,
                String filePath,
                int fileSize,
                String fileType,
                int taskID,
                int userID,
                Date createdAt,
                Date updateAt) {
        this.id = id;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.taskID = taskID;
        this.userID = userID;
        this.createdAt = createdAt;
        this.updateAt = updateAt;
    }

    protected File(Parcel in) {
        id        = in.readInt();
        fileName  = in.readString();
        filePath  = in.readString();
        fileSize  = in.readInt();
        fileType  = in.readString();
        taskID    = in.readInt();
        userID    = in.readInt();
        long ca   = in.readLong();
        createdAt = ca == -1 ? null : new Date(ca);
        long ua   = in.readLong();
        updateAt  = ua == -1 ? null : new Date(ua);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(fileName);
        dest.writeString(filePath);
        dest.writeInt(fileSize);
        dest.writeString(fileType);
        dest.writeInt(taskID);
        dest.writeInt(userID);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
        dest.writeLong(updateAt   != null ? updateAt.getTime()   : -1);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<File> CREATOR = new Creator<File>() {
        @Override
        public File createFromParcel(Parcel in) {
            return new File(in);
        }

        @Override
        public File[] newArray(int size) {
            return new File[size];
        }
    };

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public int getFileSize() { return fileSize; }
    public void setFileSize(int fileSize) { this.fileSize = fileSize; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public int getTaskID() { return taskID; }
    public void setTaskID(int taskID) { this.taskID = taskID; }

    public int getUserID() { return userID; }
    public void setUserID(int userID) { this.userID = userID; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdateAt() { return updateAt; }
    public void setUpdateAt(Date updateAt) { this.updateAt = updateAt; }
}
