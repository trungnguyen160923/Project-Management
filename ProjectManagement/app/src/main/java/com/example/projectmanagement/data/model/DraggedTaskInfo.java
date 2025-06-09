package com.example.projectmanagement.data.model;

public class DraggedTaskInfo {
    private final Task task;
    private final Phase phase;
    private final int originalPosition;
    private final int taskWidth;
    private final int taskHeight;

    public DraggedTaskInfo(Task task,
                           Phase phase,
                           int originalPosition,
                           int taskWidth,
                           int taskHeight) {
        this.task = task;
        this.phase = phase;
        this.originalPosition = originalPosition;
        this.taskWidth = taskWidth;
        this.taskHeight = taskHeight;
    }

    public Task getTask() { return task; }
    public Phase getPhase() { return phase; }
    public int getOriginalPosition() { return originalPosition; }
    public int getTaskWidth() { return taskWidth; }
    public int getTaskHeight() { return taskHeight; }
}