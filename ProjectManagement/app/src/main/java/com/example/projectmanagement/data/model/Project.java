package com.example.projectmanagement.data.model;

import com.example.projectmanagement.utils.ParseDateUtil;

import java.util.Date;

public class Project {
    private int projectID;
    private String projectName;
    private String projectDescription;
    private Date deadline;
    private String status;
    private Date createAt;
    private String AccessLevel;
    private String inviteLinkToken;
    private String backgroundImg;

    public Project() {
    }

    public Project(String projectName, String projectDescription, String date , String backgroundImg) {
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.deadline = ParseDateUtil.parseDate(date);
        this.backgroundImg = backgroundImg;
    }

    public Project(int projectID, String projectName, String projectDescription, Date deadline,
                   String status, Date createAt, String accessLevel,
                   String inviteLinkToken, String backgroundImg) {
        this.projectID = projectID;
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.deadline = deadline;
        this.status = status;
        this.createAt = createAt;
        AccessLevel = accessLevel;
        this.inviteLinkToken = inviteLinkToken;
        this.backgroundImg = backgroundImg;
    }

    public int getProjectID() {
        return projectID;
    }

    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public String getAccessLevel() {
        return AccessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        AccessLevel = accessLevel;
    }

    public String getInviteLinkToken() {
        return inviteLinkToken;
    }

    public void setInviteLinkToken(String inviteLinkToken) {
        this.inviteLinkToken = inviteLinkToken;
    }

    public String getBackgroundImg() {
        return backgroundImg;
    }

    public void setBackgroundImg(String backgroundImg) {
        this.backgroundImg = backgroundImg;
    }
}
