package com.example.projectmanagement.ui.notification;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledExecutorServiceWorker {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void start() {
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                URL url = new URL("http://192.168.0.109:3000/abc"); // localhost của emulator
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                int code = connection.getResponseCode();
                System.out.println("Response code: " + code);
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 2, TimeUnit.SECONDS); // Delay 2s sau mỗi lần chạy xong
    }

    public void stop() {
        scheduler.shutdownNow(); // Ngừng toàn bộ khi activity bị hủy
    }
}

