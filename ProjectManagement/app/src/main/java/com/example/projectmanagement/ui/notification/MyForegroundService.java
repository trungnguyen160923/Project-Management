package com.example.projectmanagement.ui.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MyForegroundService extends Service {

    private static final String CHANNEL_ID = "BackgroundRequestChannel";
    private ScheduledExecutorService scheduler;
    private String extractMessageFromJson(String json) {
        try {
            json = json.trim();
            if (json.startsWith("{") && json.endsWith("}")) {
                json = json.substring(1, json.length() - 1); // bỏ { }
                String[] parts = json.split(":");
                if (parts.length == 2) {
                    return parts[1].trim().replaceAll("^\"|\"$", ""); // bỏ dấu "
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Server Polling Channel",
                    NotificationManager.IMPORTANCE_HIGH // cần HIGH để phát âm thanh
            );

            // Bật âm thanh và rung
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setSound(
                    android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                    new android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    private void showNotification(String message) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("TeamWork")
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // đảm bảo hệ thống cho phát âm thanh
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL) // rung + âm thanh + đèn
                .build();

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify((int) System.currentTimeMillis(), notification);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Đang lấy dữ liệu từ server")
                .setContentText("Cứ 5s sẽ lấy 1 lần")
                .setSmallIcon(android.R.drawable.ic_popup_sync)
                .build();

        startForeground(1, notification);

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                URL url = new URL("http://192.168.0.109:3000/a");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int code = conn.getResponseCode();
                System.out.println("Response code: " + code);

                if (code == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    String json = response.toString();
                    System.out.println("Server response: " + json);

                    // Giả sử response là { "message": "Nội dung..." }
                    String message = extractMessageFromJson(json);

                    if (message != null && !message.isEmpty()) {
                        showNotification(message);
                    }
                }

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 5, TimeUnit.SECONDS);
    }


    @Override
    public void onDestroy() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
