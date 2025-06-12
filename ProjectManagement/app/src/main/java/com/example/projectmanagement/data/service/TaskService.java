package com.example.projectmanagement.data.service;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.projectmanagement.data.model.Task;
import com.example.projectmanagement.utils.ApiConfig;
import com.example.projectmanagement.utils.ParseDateUtil;
import com.example.projectmanagement.utils.UserPreferences;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskService {
    private static final String TAG = "TaskService";
    private static final String BASE_URL = ApiConfig.BASE_URL;

    /** Lấy danh sách tasks của phase */
    public static void getPhaseTasks(Context context,
                                   int phaseId,
                                   Response.Listener<JSONObject> listener,
                                   Response.ErrorListener errorListener) {
        String url = BASE_URL + "/tasks/phase/" + phaseId;
        
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = new UserPreferences(context).getJwtToken();
                if (token != null && !token.isEmpty()) {
                    headers.put("Cookie", "user_auth_token=" + token);
                }
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        ApiConfig.getInstance(context).addToRequestQueue(request);
    }

    public static void createTask(Context context, Task task,
                                Response.Listener<JSONObject> listener,
                                Response.ErrorListener errorListener) {
        String url = BASE_URL + "/tasks";
        
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("taskName", task.getTaskName());
            requestBody.put("description", task.getTaskDescription());
            requestBody.put("status", task.getStatus());
            requestBody.put("priority", task.getPriority());
//            requestBody.put("startDate", task.getStartDate());
//            requestBody.put("endDate", task.getEndDate());
            requestBody.put("orderIndex", task.getOrderIndex());
            
            // Add phase info
            JSONObject phaseObj = new JSONObject();
            phaseObj.put("id", task.getPhaseID());
            requestBody.put("phase", phaseObj);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                    listener, errorListener) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    String token = new UserPreferences(context).getJwtToken();
                    if (token != null && !token.isEmpty()) {
                        headers.put("Cookie", "user_auth_token=" + token);
                    }
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            ApiConfig.getInstance(context).addToRequestQueue(request);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating request body", e);
            errorListener.onErrorResponse(new VolleyError("Error creating request body"));
        }
    }

    // Parse danh sách tasks từ response
    public static List<Task> parseTasksList(JSONObject response) throws JSONException {
        List<Task> tasks = new ArrayList<>();
        JSONArray data = response.getJSONArray("data");
        
        for (int i = 0; i < data.length(); i++) {
            JSONObject taskObj = data.getJSONObject(i);
            Task task = parseTask(taskObj);
            tasks.add(task);
        }
        
        return tasks;
    }

    public static Task parseTask(JSONObject taskObj) throws JSONException {
        Task task = new Task();
        task.setTaskID(taskObj.getInt("id"));
        task.setTaskName(taskObj.getString("taskName"));
        task.setTaskDescription(taskObj.getString("description"));
        task.setStatus(taskObj.getString("status"));
        task.setPriority(taskObj.getString("priority"));
//        task.setStartDate(taskObj.getString("startDate"));
        task.setDueDate(ParseDateUtil.parseDate(taskObj.getString("endDate")));
        task.setOrderIndex(taskObj.getInt("orderIndex"));
        
        if (taskObj.has("phase")) {
            JSONObject phaseObj = taskObj.getJSONObject("phase");
            task.setPhaseID(phaseObj.getInt("id"));
        }
        
        return task;
    }
}
