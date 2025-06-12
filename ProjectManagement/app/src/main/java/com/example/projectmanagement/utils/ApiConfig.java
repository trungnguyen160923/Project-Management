package com.example.projectmanagement.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class ApiConfig {
    private static final String TAG = "ApiConfig";
    public static final String BASE_URL = "http://192.168.0.111:8080/api"; // Android emulator localhost
    private static ApiConfig instance;
    private final RequestQueue requestQueue;

    private ApiConfig(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        Log.d(TAG, "ApiConfig initialized with base URL: " + BASE_URL);
    }

    public static synchronized ApiConfig getInstance(Context context) {
        if (instance == null) {
            instance = new ApiConfig(context);
        }
        return instance;
    }

    public void addToRequestQueue(com.android.volley.Request<?> request) {
        requestQueue.add(request);
    }

    public void cancelAllRequests() {
        requestQueue.cancelAll(request -> true);
    }
} 