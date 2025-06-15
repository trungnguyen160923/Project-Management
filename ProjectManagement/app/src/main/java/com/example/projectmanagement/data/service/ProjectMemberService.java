package com.example.projectmanagement.data.service;

import static com.example.projectmanagement.utils.ApiConfig.BASE_URL;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.projectmanagement.utils.ApiConfig;
import com.example.projectmanagement.utils.UserPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectMemberService {
    private static final String TAG = "ProjectMemberService";
    private static final String PROJECT_MEMBER_URL = BASE_URL + "/members";  // Bỏ dấu / ở cuối

    public static void fetchProjectMembers(
            Context context,
            int projectId,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {
        String url = PROJECT_MEMBER_URL + "/projects/" + projectId;

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

    public static void addMember(
            Context context,
            int taskId,
            int userId,
            int projectId,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener
    ) {
        // 1. Build URL with query-string parameters
        String url = PROJECT_MEMBER_URL
                + "/add-member"  // Không còn 2 dấu // liên tiếp
                + "?taskId=" + taskId
                + "&userId=" + userId
                + "&projectId=" + projectId;

        Log.d(TAG, "Adding member with URL: " + url);
        Log.d(TAG, "TaskId: " + taskId + ", UserId: " + userId + ", ProjectId: " + projectId);

        // 2. Since the server doesn't expect a JSON body, pass `null` as the body
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null,    // no JSON payload
                response -> {
                    Log.d(TAG, "Add member success: " + response.toString());
                    listener.onResponse(response);
                },
                error -> {
                    Log.e(TAG, "Add member error: " + error.toString());
                    errorListener.onErrorResponse(error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = new UserPreferences(context).getJwtToken();
                Log.d(TAG, "Using token: " + token);
                headers.put("Cookie", "user_auth_token=" + token);
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        request.setShouldCache(false);
        Volley.newRequestQueue(context).add(request);
    }

    public static void removeMemberInTask(
            Context context,
            int taskId,
            int userId,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener
    ) {
        // Build URL with query parameters
        String url = PROJECT_MEMBER_URL
                + "/remove-member"
                + "?taskId=" + taskId
                + "&userId=" + userId;

        // Create the request (no JSON body needed)
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,  // or DELETE if your API expects DELETE
                url,
                null,                 // no body, query params only
                listener,
                errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // Send your auth token as before
                String token = new UserPreferences(context).getJwtToken();
                headers.put("Cookie", "user_auth_token=" + token);
                return headers;
            }

            @Override
            public String getBodyContentType() {
                // Body is null, but you can specify default
                return "application/json; charset=utf-8";
            }
        };

        // Disable caching for mutation endpoints
        request.setShouldCache(false);

        // Enqueue the request
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }

    public static void sendProjectInvitation(
            Context context,
            int projectId,
            List<Integer> userIds,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {

        String url = BASE_URL + "/projects/" + projectId + "/invite";

        // Tạo JSON body
        JSONObject requestBody = new JSONObject();
        try {
            JSONArray jsonArray = new JSONArray();
            for (Integer id : userIds) {
                jsonArray.put(id);
            }
            requestBody.put("userIds", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // Tạo request
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                listener,
                errorListener
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

    public static void changeRole(Context context, int projectId, int userId, String role,
                                Response.Listener<JSONObject> listener,
                                Response.ErrorListener errorListener) {
        String url = PROJECT_MEMBER_URL + "/change-role";
        
        // Add query parameters
        url += "?projectId=" + projectId + "&userId=" + userId + "&role=" + role;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                null,
                listener,
                errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                UserPreferences prefs = new UserPreferences(context);
                String token = prefs.getJwtToken();
                headers.put("Cookie", "user_auth_token=" + token);
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }

    public static void removeMember(Context context, int projectId, int userId,
                                Response.Listener<JSONObject> listener,
                                Response.ErrorListener errorListener) {
        String url = PROJECT_MEMBER_URL + "/remove-project-member";
        
        // Add query parameters
        url += "?projectId=" + projectId + "&userId=" + userId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                listener,
                errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                UserPreferences prefs = new UserPreferences(context);
                String token = prefs.getJwtToken();
                headers.put("Cookie", "user_auth_token=" + token);
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }
}
