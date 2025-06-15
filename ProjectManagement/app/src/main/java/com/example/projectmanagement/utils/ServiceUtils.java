package com.example.projectmanagement.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

public class ServiceUtils {
    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true; // Service đang chạy
            }
        }

        return false; // Chưa chạy
    }
}
