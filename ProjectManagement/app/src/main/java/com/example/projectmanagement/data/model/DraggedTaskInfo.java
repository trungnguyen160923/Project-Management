package com.example.projectmanagement.data.model;

public class DraggedTaskInfo {
    public final Task task;
    public final Phase sourcePhase;
    public final int originalPosition;
    public final int viewWidth;
    public final int viewHeight;

    public DraggedTaskInfo(Task task, Phase sourcePhase,
                           int originalPosition,
                           int viewWidth, int viewHeight) {
        this.task = task;
        this.sourcePhase = sourcePhase;
        this.originalPosition = originalPosition;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
    }
}