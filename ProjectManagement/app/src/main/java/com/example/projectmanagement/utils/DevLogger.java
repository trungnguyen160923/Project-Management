// File: FileLogger.java
package com.example.projectmanagement.utils;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class DevLogger {
    public static void logToFile(String message) {
        // Trong Activity hoặc bất cứ chỗ nào có Context
        Context context = null;
        File logDir = new File(context.getFilesDir(), "logs");
        if (!logDir.exists()) logDir.mkdirs();

        File logFile = new File(logDir, "dev.log");
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
            writer.println(">>> " + message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
