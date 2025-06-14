package com.example.projectmanagement.data.model;

import java.util.List;

public class Statistics {
    private int ownedProjectsCount;
    private int joinedProjectsCount;
    private int totalProjectsCount;
    private int totalTasksCount;
    private int completedTasksCount;
    private int pendingTasksCount;
    private int assignedTasksCount;
    private int completedAssignedTasksCount;
    private List<ProjectMemberStat> projectMemberStats;
    private List<PhaseStat> phaseStats;
    private List<ProjectTaskStat> projectTaskStats;

    public static class ProjectMemberStat {
        private int projectId;
        private String projectName;
        private int memberCount;

        public int getProjectId() { return projectId; }
        public void setProjectId(int projectId) { this.projectId = projectId; }
        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }
        public int getMemberCount() { return memberCount; }
        public void setMemberCount(int memberCount) { this.memberCount = memberCount; }
    }

    public static class PhaseStat {
        private int projectId;
        private String projectName;
        private int phaseCount;

        public int getProjectId() { return projectId; }
        public void setProjectId(int projectId) { this.projectId = projectId; }
        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }
        public int getPhaseCount() { return phaseCount; }
        public void setPhaseCount(int phaseCount) { this.phaseCount = phaseCount; }
    }

    public static class ProjectTaskStat {
        private int projectId;
        private String projectName;
        private int totalAssignedTasks;
        private int completedAssignedTasks;

        public int getProjectId() { return projectId; }
        public void setProjectId(int projectId) { this.projectId = projectId; }
        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }
        public int getTotalAssignedTasks() { return totalAssignedTasks; }
        public void setTotalAssignedTasks(int totalAssignedTasks) { this.totalAssignedTasks = totalAssignedTasks; }
        public int getCompletedAssignedTasks() { return completedAssignedTasks; }
        public void setCompletedAssignedTasks(int completedAssignedTasks) { this.completedAssignedTasks = completedAssignedTasks; }
    }

    // Getters and Setters
    public int getOwnedProjectsCount() { return ownedProjectsCount; }
    public void setOwnedProjectsCount(int ownedProjectsCount) { this.ownedProjectsCount = ownedProjectsCount; }
    public int getJoinedProjectsCount() { return joinedProjectsCount; }
    public void setJoinedProjectsCount(int joinedProjectsCount) { this.joinedProjectsCount = joinedProjectsCount; }
    public int getTotalProjectsCount() { return totalProjectsCount; }
    public void setTotalProjectsCount(int totalProjectsCount) { this.totalProjectsCount = totalProjectsCount; }
    public int getTotalTasksCount() { return totalTasksCount; }
    public void setTotalTasksCount(int totalTasksCount) { this.totalTasksCount = totalTasksCount; }
    public int getCompletedTasksCount() { return completedTasksCount; }
    public void setCompletedTasksCount(int completedTasksCount) { this.completedTasksCount = completedTasksCount; }
    public int getPendingTasksCount() { return pendingTasksCount; }
    public void setPendingTasksCount(int pendingTasksCount) { this.pendingTasksCount = pendingTasksCount; }
    public List<ProjectMemberStat> getProjectMemberStats() { return projectMemberStats; }
    public void setProjectMemberStats(List<ProjectMemberStat> projectMemberStats) { this.projectMemberStats = projectMemberStats; }
    public List<PhaseStat> getPhaseStats() { return phaseStats; }
    public void setPhaseStats(List<PhaseStat> phaseStats) { this.phaseStats = phaseStats; }
    public int getAssignedTasksCount() { return assignedTasksCount; }
    public void setAssignedTasksCount(int assignedTasksCount) { this.assignedTasksCount = assignedTasksCount; }
    public int getCompletedAssignedTasksCount() { return completedAssignedTasksCount; }
    public void setCompletedAssignedTasksCount(int completedAssignedTasksCount) { this.completedAssignedTasksCount = completedAssignedTasksCount; }
    public List<ProjectTaskStat> getProjectTaskStats() { return projectTaskStats; }
    public void setProjectTaskStats(List<ProjectTaskStat> projectTaskStats) { this.projectTaskStats = projectTaskStats; }
} 