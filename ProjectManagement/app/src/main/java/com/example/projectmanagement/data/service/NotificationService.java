package com.example.projectmanagement.data.service;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.os.Handler;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.projectmanagement.api.ApiClient;
import com.example.projectmanagement.utils.ApiConfig;
import com.example.projectmanagement.utils.UserPreferences;

import org.json.JSONObject;

public class NotificationService {
    private static final String BASE_URL = ApiConfig.BASE_URL;
    private static final String NOTICATION_URL = BASE_URL + "/notifications";
    private static final String TAG = "UserService";
    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static Runnable notificationFetchRunnable;

    public static void fetchNotifications(Context context, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = NOTICATION_URL;
        Log.d(TAG, ">>> fetch notifications");
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    listener.onResponse(response);
                },
                error -> {
                    errorListener.onErrorResponse(error);
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                UserPreferences prefs = new UserPreferences(context);
                String token = prefs.getJwtToken();
                headers.put("Cookie", "user_auth_token=" + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }

    public static void startRecursiveFetchNotifications(Context context,
                                                        Response.Listener<JSONObject> listener,
                                                        Response.ErrorListener errorListener) {
        Log.d(TAG, ">>> Starting recursive fetch notifications");

        if (notificationFetchRunnable != null) {
            stopRecursiveFetchNotifications();
            startRecursiveFetchNotifications(context, listener, errorListener);
        }

        notificationFetchRunnable = new Runnable() {
            @Override
            public void run() {
                fetchNotifications(context, response -> {
                    // Gọi lại listener bên ngoài
                    listener.onResponse(response);

                    // Gọi lại hàm sau 2 giây
                    handler.postDelayed(notificationFetchRunnable, 2000);
                }, errorListener);
            }
        };

        // Bắt đầu gọi lần đầu tiên
        handler.post(notificationFetchRunnable);
    }

    public static void stopRecursiveFetchNotifications() {
        Log.d(TAG, ">>> Stopping recursive fetch notifications");
        if (notificationFetchRunnable != null) {
            handler.removeCallbacks(notificationFetchRunnable);
            notificationFetchRunnable = null;
        }
    }

    public static void markAllAsRead(Context context, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = NOTICATION_URL + "/mark-all-as-read";
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                null,
                response -> {
                    listener.onResponse(response);
                },
                error -> {
                    errorListener.onErrorResponse(error);
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                UserPreferences prefs = new UserPreferences(context);
                String token = prefs.getJwtToken();
                headers.put("Cookie", "user_auth_token=" + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }

    public static void markAsRead(Context context, Long notificationId, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = NOTICATION_URL + "/" + notificationId + "/read";
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                null,
                response -> {
                    listener.onResponse(response);
                },
                error -> {
                    errorListener.onErrorResponse(error);
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                UserPreferences prefs = new UserPreferences(context);
                String token = prefs.getJwtToken();
                headers.put("Cookie", "user_auth_token=" + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }

}
