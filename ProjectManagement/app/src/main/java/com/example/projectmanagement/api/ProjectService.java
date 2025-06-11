package com.example.projectmanagement.api;

import android.content.Context;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.projectmanagement.utils.UserPreferences;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.utils.ParseDateUtil;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectService {
    private static final String API_PREFIX = "http://10.0.2.2:8080/api";
    private static final String TAG = "ProjectService";

    /**
     * Lấy JWT từ SharedPreferences và đính kèm vào header Authorization
     */
    private static Map<String, String> getAuthHeaders(Context context) {
        Map<String, String> headers = new HashMap<>();
        UserPreferences prefs = new UserPreferences(context);
        String token = prefs.getJwtToken();
        if (token != null && !token.isEmpty()) {
            headers.put("Cookie", "user_auth_token=" + token);
            Log.d(TAG, "JWT added to Authorization header");
        } else {
            Log.w(TAG, "JWT token is empty or missing");
        }
        return headers;
    }

    /**
     * Tạo request chung với JsonObjectRequest, đính kèm headers và retry policy
     */
    private static JsonObjectRequest makeRequest(int method,
                                                 String endpoint,
                                                 final JSONObject body,
                                                 final Context context,
                                                 Response.Listener<JSONObject> listener,
                                                 Response.ErrorListener errorListener) {
        String url = API_PREFIX + endpoint;
        JsonObjectRequest request = new JsonObjectRequest(method, url, body,
                listener, error -> { /* xử lý lỗi như cũ */ }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                if (body != null) {
                    headers.put("Content-Type", "application/json; charset=utf-8");
                }
                // Cookie không có khoảng trắng thừa sau '='
                String token = new UserPreferences(context).getJwtToken();
                if (token != null && !token.isEmpty()) {
                    headers.put("Cookie", "user_auth_token=" + token);
                }
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        return request;
    }


    /** Tạo project mới */
    public static void createProject(Context ctx,
                                     Project project,
                                     Response.Listener<JSONObject> listener,
                                     Response.ErrorListener errorListener) {
        try {
            JSONObject body = new JSONObject();
            body.put("projectName", project.getProjectName());
            body.put("description", project.getProjectDescription());
            body.put("status", "ACTIVE");
            
            // Format dates in ISO 8601 format
            String startDate = ParseDateUtil.formatDate(project.getStartDate());
            String endDate = ParseDateUtil.formatDate(project.getDeadline());
            
            // Convert to ISO format if needed
            if (startDate.contains("/")) {
                startDate = startDate.replace("/", "-");
            }
            if (endDate.contains("/")) {
                endDate = endDate.replace("/", "-");
            }
            
            body.put("startDate", startDate);
            body.put("endDate", endDate);
            body.put("backgroundImg", project.getBackgroundImg());

            Log.d(TAG, "Creating project with data: " + body.toString());

            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.POST,
                    API_PREFIX + "/projects",
                    body,
                    response -> {
                        Log.d(TAG, "Project creation response: " + response.toString());
                        listener.onResponse(response);
                    },
                    error -> {
                        Log.e(TAG, "Error creating project: " + error.toString());
                        if (error.networkResponse != null) {
                            try {
                                String bodys = new String(error.networkResponse.data, "UTF-8");
                                Log.e(TAG, "Error response body: " + bodys);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing error response", e);
                            }
                        }
                        errorListener.onErrorResponse(error);
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Accept", "application/json");
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    String token = new UserPreferences(ctx).getJwtToken();
                    if (token != null && !token.isEmpty()) {
                        headers.put("Cookie", "user_auth_token=" + token);
                    }
                    return headers;
                }
            };

            req.setRetryPolicy(new DefaultRetryPolicy(
                    10000, // 10 seconds timeout
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            Log.d(TAG, "Adding request to queue");
            ApiClient.getInstance(ctx).addToRequestQueue(req);
            Log.d(TAG, "Request added to queue");
        } catch (JSONException e) {
            Log.e(TAG, "Error creating project request", e);
            errorListener.onErrorResponse(new VolleyError("Error creating request: " + e.getMessage()));
        }
    }


    /** Lấy danh sách tất cả project */
    public static void getAllProjects(Context ctx,
                                      Response.Listener<JSONObject> listener,
                                      Response.ErrorListener errorListener) {
        JsonObjectRequest req = makeRequest(
                Request.Method.GET,
                "/projects",
                null,
                ctx,
                listener,
                errorListener
        );
        ApiClient.getInstance(ctx).addToRequestQueue(req);
    }


    /** Lấy chi tiết project theo ID */
    public static void getProjectDetail(Context context,
                                        String projectId,
                                        Response.Listener<JSONObject> listener,
                                        Response.ErrorListener errorListener) {
        JsonObjectRequest req = makeRequest(
                Request.Method.GET,
                "/projects/" + projectId,
                null,
                context,
                listener,
                errorListener
        );
        ApiClient.getInstance(context).addToRequestQueue(req);
    }

    /** Cập nhật project */
    public static void updateProject(Context context,
                                     String projectId,
                                     JSONObject projectData,
                                     Response.Listener<JSONObject> listener,
                                     Response.ErrorListener errorListener) {
        JsonObjectRequest req = makeRequest(
                Request.Method.PUT,
                "/projects/" + projectId,
                projectData,
                context,
                listener,
                errorListener
        );
        ApiClient.getInstance(context).addToRequestQueue(req);
    }

    /** Xóa project */
    public static void deleteProject(Context context,
                                     String projectId,
                                     Response.Listener<JSONObject> listener,
                                     Response.ErrorListener errorListener) {
        JsonObjectRequest req = makeRequest(
                Request.Method.DELETE,
                "/projects/" + projectId,
                null,
                context,
                listener,
                errorListener
        );
        ApiClient.getInstance(context).addToRequestQueue(req);
    }

    // Parse danh sách projects từ response
    public static List<Project> parseProjectsList(JSONObject response) throws JSONException {
        List<Project> projects = new ArrayList<>();
        JSONArray data = response.optJSONArray("data");
        if (data != null) {
            for (int i = 0; i < data.length(); i++) {
                JSONObject projectJson = data.getJSONObject(i);
                Project project = parseProject(projectJson);
                projects.add(project);
            }
        }
        return projects;
    }

    // Parse một project từ JSON
    public static Project parseProject(JSONObject json) throws JSONException {
        Project project = new Project();
        project.setProjectID(json.optInt("id", -1));
        project.setProjectName(json.optString("projectName", ""));
        project.setProjectDescription(json.optString("description", ""));
        project.setStatus(json.optString("status", ""));
        project.setStartDate(ParseDateUtil.parseDate(json.optString("startDate", "")));
        project.setDeadline(ParseDateUtil.parseDate(json.optString("endDate", "")));
        project.setCreateAt(ParseDateUtil.parseDate(json.optString("createdAt", "")));
        project.setUpdateAt(ParseDateUtil.parseDate(json.optString("updatedAt", "")));
        project.setBackgroundImg(json.optString("backgroundImg", ""));
        return project;
    }
}