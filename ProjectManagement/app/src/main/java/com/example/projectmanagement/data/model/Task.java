package com.example.projectmanagement.data.model;

import com.example.projectmanagement.utils.ParseDateUtil;

import java.util.Date;

public class Task {
    private int taskID;
    private int phaseID;
    private String taskName;
    private String taskDescription;
    private int assignedTo;
    private String status;
    private String priority;
    private Date dueDate;
    private Boolean allowSelfAssign;
    private int orderIndex;
    private Date createAt;
    private Date lastUpdate;

    private Integer cmtCnt, fileCnt;

    public Task() {

    }

    public Task(int taskID, int phaseID, String taskName, String taskDescription,
                int assignedTo, String status, String priority, Date dueDate,
                Boolean allowSelfAssign, Date createAt, int orderIndex, Integer cmtCnt, Integer fileCnt) {
        this.taskID = taskID;
        this.phaseID = phaseID;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.assignedTo = assignedTo;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.allowSelfAssign = allowSelfAssign;
        this.createAt = createAt;
        this.orderIndex = orderIndex;
        this.cmtCnt = cmtCnt;
        this.fileCnt = fileCnt;
    }

    public Task(String taskName, String taskDescription, String status, String dueDate, Integer cmtCnt, Integer fileCnt) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.status = status;
        this.dueDate = ParseDateUtil.parseDate(dueDate);
        this.cmtCnt = cmtCnt;
        this.fileCnt = fileCnt;
    }

    public Task(int taskID, int phaseID, String taskName, String taskDescription,
                int assignedTo, String status, String priority, Date dueDate,
                Boolean allowSelfAssign, int orderIndex,
                Date createAt, Date lastUpdate,
                Integer cmtCnt, Integer fileCnt) {
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
        this.cmtCnt = cmtCnt;
        this.fileCnt = fileCnt;
    }

    public int getTaskID() {
        return taskID;
    }

    public void setTaskID(int taskID) {
        this.taskID = taskID;
    }

    public int getPhaseID() {
        return phaseID;
    }

    public void setPhaseID(int phaseID) {
        this.phaseID = phaseID;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public int getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(int assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Boolean getAllowSelfAssign() {
        return allowSelfAssign;
    }

    public void setAllowSelfAssign(Boolean allowSelfAssign) {
        this.allowSelfAssign = allowSelfAssign;
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

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Integer getCmtCnt() {
        return cmtCnt;
    }

    public void setCmtCnt(Integer cmtCnt) {
        this.cmtCnt = cmtCnt;
    }

    public Integer getFileCnt() {
        return fileCnt;
    }

    public void setFileCnt(Integer fileCnt) {
        this.fileCnt = fileCnt;
    }
}
