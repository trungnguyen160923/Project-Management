package com.example.projectmanagement.data.convertor;

import com.example.projectmanagement.data.model.Comment;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CommentConvertor {
    public static Comment fromJson(JSONObject json) throws JSONException {
        int id = json.getInt("id");
        String content = json.getString("content");
        int taskID = json.getInt("taskId");
        int userID = json.getInt("userId");
        String username = json.getString("username");
        String createdAtStr = json.getString("createdAt");
        String updatedAtStr = json.getString("updatedAt");
        String userRole = json.getString("userRole");
        boolean isTaskResult = json.getBoolean("isTaskResult");

        // Parse ISO8601 datetime string (e.g., "2025-06-13T13:00:12.123")
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date createdAt = null;
        Date updatedAt = null;
        try {
            createdAt = formatter.parse(createdAtStr);
            updatedAt = formatter.parse(updatedAtStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return new Comment(id, content, taskID, userID, createdAt, updatedAt);
    }
}