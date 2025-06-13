package com.example.projectmanagement.data.repository;

import android.content.Context;

import com.android.volley.VolleyError;
import com.example.projectmanagement.data.service.NotificationService;
import com.example.projectmanagement.utils.CustomCallback;

import org.json.JSONObject;

public class NotificationRepository {
    private final Context context;

    public NotificationRepository(Context context) {
        this.context = context;
    }

    public void fetchNotifications(CustomCallback<JSONObject, VolleyError> callback) {
        NotificationService.startRecursiveFetchNotifications(context, callback::onSuccess, callback::onError);
    }
}

