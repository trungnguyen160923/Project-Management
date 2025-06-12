package com.example.projectmanagement.data.model;

import android.util.Log;

// ProjectHolder.java
public class ProjectHolder {
    private static final String TAG = "ProjectHolder";
    private static Project project;

    // Đặt project hiện tại
    public static void set(Project p) {
        Log.d(TAG, "Setting project: id=" + (p != null ? p.getProjectID() : "null") + 
            ", name=" + (p != null ? p.getProjectName() : "null"));
        project = p;
    }

    // Lấy project hiện tại
    public static Project get() {
        Log.d(TAG, "Getting project: id=" + (project != null ? project.getProjectID() : "null") + 
            ", name=" + (project != null ? project.getProjectName() : "null"));
        return project;
    }

    // Xoá project hiện tại (khi logout, đổi user, v.v.)
    public static void clear() {
        Log.d(TAG, "Clearing project");
        project = null;
    }

    // Kiểm tra đã có project hay chưa
    public static boolean hasProject() {
        return project != null;
    }
}

