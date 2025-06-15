package com.example.projectmanagement.data.service;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.projectmanagement.api.ApiClient;
import com.example.projectmanagement.utils.ApiConfig;
import com.example.projectmanagement.utils.DevLogger;
import com.example.projectmanagement.utils.UserPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CommentService {
    private static final String TAG = "CommentService";

    public static void createComment(
            Context context,
            long taskId,
            String content,
            boolean isTaskResult,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {

        String url = ApiConfig.BASE_URL + "/comments/" + taskId;

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("content", content); // comment content
            requestBody.put("isTaskResult", isTaskResult); // is task result
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                listener,
                errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                UserPreferences prefs = new UserPreferences(context);
                String token = prefs.getJwtToken();
                headers.put("Cookie", "user_auth_token=" + token); // Add your JWT in cookie header
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }

    public static void fetchCommentsByTaskId(
            Context context,
            long taskId,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {

        String url = ApiConfig.BASE_URL + "/comments/task/" + taskId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                listener,
                errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                UserPreferences prefs = new UserPreferences(context);
                String token = prefs.getJwtToken();
                headers.put("Cookie", "user_auth_token=" + token); // gửi token nếu server dùng cookie auth
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }

    public static void updateComment(Context context, long commentId, boolean isTaskResult, String content,
                                     Response.Listener<JSONObject> listener,
                                     Response.ErrorListener errorListener) {

        String url = ApiConfig.BASE_URL + "/comments/" + commentId;

        JSONObject body = new JSONObject();
        try {
            body.put("isTaskResult", isTaskResult);
            body.put("content", content);
        } catch (JSONException e) {
            e.printStackTrace();
            errorListener.onErrorResponse(new VolleyError("Lỗi tạo JSON request"));
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                response -> {
                    listener.onResponse(response);
                },
                error -> {
                    errorListener.onErrorResponse(error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
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

    public static void deleteComment(Context context, long commentId,
                                     Response.Listener<JSONObject> listener,
                                     Response.ErrorListener errorListener) {

        String url = ApiConfig.BASE_URL + "/comments/" + commentId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,  // DELETE không cần body
                response -> {
                    listener.onResponse(response);
                },
                error -> {
                    errorListener.onErrorResponse(error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
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

    public static void markCommentAsTaskResult(
            Context context,
            long commentId,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener
    ) {
        String url = ApiConfig.BASE_URL + "/comments/" + commentId + "/mark-as-task-result";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                null, // Không có body
                response -> {
                    listener.onResponse(response);
                },
                error -> {
                    errorListener.onErrorResponse(error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
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
