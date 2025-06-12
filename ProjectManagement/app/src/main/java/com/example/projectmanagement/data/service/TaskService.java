package com.example.projectmanagement.data.service;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.data.model.ProjectHolder;
import com.example.projectmanagement.data.model.Task;
import com.example.projectmanagement.utils.ApiConfig;
import com.example.projectmanagement.utils.ParseDateUtil;
import com.example.projectmanagement.utils.UserPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TaskService {
    private static final String TAG = "TaskService";
    private static final String BASE_URL = ApiConfig.BASE_URL;
    private static final String TASKS_URL = BASE_URL + "/tasks";

    public static void createTask(Context context, Task task,
                                Response.Listener<JSONObject> listener,
                                Response.ErrorListener errorListener) {
        try {
            JSONObject requestBody = new JSONObject();
            JSONObject taskObject = new JSONObject();
            
            // Set task properties
            taskObject.put("taskName", task.getTaskName());
            taskObject.put("description", task.getTaskDescription());
            taskObject.put("status", task.getStatus());
            taskObject.put("priority", task.getPriority());
            taskObject.put("allowSelfAssign", true);
            taskObject.put("orderIndex", task.getOrderIndex());
            
            // Format date to ISO-8601 format
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            outputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            String formattedDate = outputFormat.format(task.getDueDate());
            taskObject.put("dueDate", formattedDate);
            
            // Set phase information
            JSONObject phaseObject = new JSONObject();
            phaseObject.put("id", task.getPhase().getPhaseID());
            phaseObject.put("phaseName", task.getPhase().getPhaseName());
            phaseObject.put("description", task.getPhase().getDescription());
            phaseObject.put("status", task.getPhase().getStatus());
            phaseObject.put("orderIndex", task.getPhase().getOrderIndex());
            
            // Set project in phase
            JSONObject projectObject = new JSONObject();
            projectObject.put("id", ProjectHolder.get().getProjectID());
            phaseObject.put("project", projectObject);
            
            taskObject.put("phase", phaseObject);
            requestBody.put("task", taskObject);
            requestBody.put("projectId", ProjectHolder.get().getProjectID());

            Log.d(TAG, "Creating task with request body: " + requestBody.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                TASKS_URL,
                requestBody,
                response -> {
                    Log.d(TAG, "Response received: " + response.toString());
                    listener.onResponse(response);
                },
                error -> {
                    Log.e(TAG, "Error creating task", error);
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

        } catch (JSONException e) {
            Log.e(TAG, "Error creating request body", e);
            errorListener.onErrorResponse(new VolleyError("Error creating request body: " + e.getMessage()));
        }
    }

    public static void getTaskDetail(Context context, int taskId,
                                  Response.Listener<JSONObject> listener,
                                  Response.ErrorListener errorListener) {
        String url = TASKS_URL + "/" + taskId;
        Log.d(TAG, "Fetching task details from: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            response -> {
                Log.d(TAG, "Task detail response: " + response.toString());
                listener.onResponse(response);
            },
            error -> {
                Log.e(TAG, "Error fetching task details", error);
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

    public static void markTaskAsComplette(Context context, int taskId, String status,Response.Listener<JSONObject> listener,
                                           Response.ErrorListener errorListener){
        String url = TASKS_URL + "/"+taskId+"/mark-as-complete?status="+status;
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

    public static void getProjectMembers(Context context, int projectId,
                                         Response.Listener<JSONObject> listener,
                                         Response.ErrorListener errorListener) {
        String url = BASE_URL + "/api/members/projects/" + projectId;
        Log.d(TAG, "Fetching project members from: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            response -> {
                Log.d(TAG, "Project members response: " + response.toString());
                listener.onResponse(response);
            },
            error -> {
                Log.e(TAG, "Error fetching project members", error);
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

    public static void getUserInfo(Context context, int userId,
                                 Response.Listener<JSONObject> listener,
                                 Response.ErrorListener errorListener) {
        String url = BASE_URL + "/api/users/" + userId;
        Log.d(TAG, "Fetching user info from: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            response -> {
                Log.d(TAG, "User info response: " + response.toString());
                listener.onResponse(response);
            },
            error -> {
                Log.e(TAG, "Error fetching user info", error);
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
