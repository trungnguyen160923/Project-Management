package com.example.projectmanagement.data.model;
import java.util.Date;

public class Notification {
    private Long notificationId;
    private String action;
    private Date createdAt;
    private Boolean isRead;
    private String message;
    private String type;
    private Long projectId;
    private Long senderId;
    private Long userId;

    public Notification() {
    }

    public Notification(Long notificationId,
                        String action,
                        Date createdAt,
                        Boolean isRead,
                        String message,
                        String type,
                        Long projectId,
                        Long senderId,
                        Long userId) {
        this.notificationId = notificationId;
        this.action = action;
        this.createdAt = createdAt;
        this.isRead = isRead;
        this.message = message;
        this.type = type;
        this.projectId = projectId;
        this.senderId = senderId;
        this.userId = userId;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "notificationId=" + notificationId +
                ", action='" + action + '\'' +
                ", createdAt=" + createdAt +
                ", isRead=" + isRead +
                ", message='" + message + '\'' +
                ", type='" + type + '\'' +
                ", projectId=" + projectId +
                ", senderId=" + senderId +
                ", userId=" + userId +
                '}';
    }
}

