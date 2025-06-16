package com.example.projectmanagement.ui.notification;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.projectmanagement.utils.ApiConfig;
import com.example.projectmanagement.utils.UserPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MyForegroundService extends Service {

    private String TAG = "MyForegroundService";
    private static final String CHANNEL_ID = "BackgroundRequestChannel";
    private ScheduledExecutorService scheduler;
    public Context context;

    private List<String> extractMessageFromJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray msgs = jsonObject.optJSONArray("messages");
            if (msgs.equals("null") || msgs == null) {
                return null;
            }
            List<String> convertedMsgs = new ArrayList<>();
            for (int i = 0; i < msgs.length(); i++) {
                convertedMsgs.add(msgs.getString(i));
            }
            return convertedMsgs;
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
                URL url = new URL(ApiConfig.BASE_URL + "/notifications/background-service");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                UserPreferences prefs = new UserPreferences(getApplicationContext());
                String token = prefs.getJwtToken();
                conn.setRequestProperty("Cookie", "user_auth_token=" + token);

                conn.setRequestMethod("GET");

                int code = conn.getResponseCode();
                Log.d(TAG, ">>> code: " + code);

                if (code == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    String json = response.toString();
                    Log.d(TAG, ">>> msg2: " + json);
                    // Giả sử response là { "message": "Nội dung..." }
                    List<String> messages = extractMessageFromJson(json);
                    for (String message : messages) {
                        if (message != null && !message.isEmpty()) {
                            showNotification(message);
                        }
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
