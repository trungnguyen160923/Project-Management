package com.example.projectmanagement.data.model;

import java.util.Date;

public class ProjectMember {
    int projectID;
    int memberID;
    int userID;
    Date joinAt;

    public enum Role {
        ADMIN,
        LEADER,
        MEMBER
    }

    private Role role;
    public ProjectMember(int projectID,
                         int memberID,
                         int userID,
                         Date joinAt,
                         Role role) {
        this.projectID = projectID;
        this.memberID  = memberID;
        this.userID    = userID;
        this.joinAt    = joinAt;
        this.role      = role;
    }
    public int getProjectID() { return projectID; }
    public void setProjectID(int projectID) { this.projectID = projectID; }

    public int getMemberID() { return memberID; }
    public void setMemberID(int memberID) { this.memberID = memberID; }

    public int getUserID() { return userID; }
    public void setUserID(int userID) { this.userID = userID; }

    public Date getJoinAt() { return joinAt; }
    public void setJoinAt(Date joinAt) { this.joinAt = joinAt; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
