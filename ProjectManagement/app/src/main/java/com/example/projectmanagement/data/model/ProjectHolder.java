package com.example.projectmanagement.data.model;

// ProjectHolder.java
public class ProjectHolder {
    private static Project project;

    // Đặt project hiện tại
    public static void set(Project p) {
        project = p;
    }

    // Lấy project hiện tại
    public static Project get() {
        return project;
    }

    // Xoá project hiện tại (khi logout, đổi user, v.v.)
    public static void clear() {
        project = null;
    }

    // Kiểm tra đã có project hay chưa
    public static boolean hasProject() {
        return project != null;
    }
}

