package com.example.projectmanagement.data.model;

import java.util.Date;

public class Comment {
    int id;
    String content;
    int taskID;
    int userID;
    Date createAt;
    Date updateAt;

    public Comment() {
    }

    public Comment(int id, String content, int taskID, int userID, Date createAt) {
        this.id = id;
        this.content = content;
        this.taskID = taskID;
        this.userID = userID;
        this.createAt = createAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getTaskID() {
        return taskID;
    }

    public void setTaskID(int taskID) {
        this.taskID = taskID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public Date getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Date updateAt) {
        this.updateAt = updateAt;
    }
}
