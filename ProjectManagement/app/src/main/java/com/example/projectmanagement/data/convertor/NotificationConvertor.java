package com.example.projectmanagement.data.convertor;

import android.util.Log;

import com.example.projectmanagement.data.model.Comment;
import com.example.projectmanagement.data.model.Notification;
import com.example.projectmanagement.utils.ParseDateUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class NotificationConvertor {
    public static Notification fromJson(JSONObject json) throws JSONException {
        Notification notification = new Notification();
        notification.setNotificationId(json.optLong("notificationId"));
        notification.setAction(json.optString("action"));
        notification.setMessage(json.optString("message"));
        notification.setType(json.optString("type"));
        notification.setIsRead(json.optBoolean("read"));
        notification.setProjectId(json.optLong("projectId"));
        notification.setSenderId(json.optLong("senderId"));
        notification.setUserId(json.optLong("userId"));
        notification.setCreatedAt(ParseDateUtil.parseFlexibleIsoDate(json.optString("createdAt")));
        return notification;
    }
}