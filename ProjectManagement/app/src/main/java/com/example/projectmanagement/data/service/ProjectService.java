package com.example.projectmanagement.data.service;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.data.model.Task;
import com.example.projectmanagement.data.model.User;
import com.example.projectmanagement.utils.UserPreferences;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.utils.ParseDateUtil;
import com.example.projectmanagement.utils.ApiConfig;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

public class ProjectService {
    private static final String TAG = "ProjectService";
    private static final String BASE_URL = ApiConfig.BASE_URL;

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
    static JsonObjectRequest makeRequest(int method,
                                         String endpoint,
                                         final JSONObject body,
                                         final Context context,
                                         Response.Listener<JSONObject> listener,
                                         Response.ErrorListener errorListener) {
        String url = BASE_URL + endpoint;
        Log.d(TAG, "Making request to: " + url + " with method: " + method);
        if (body != null) {
            Log.d(TAG, "Request body: " + body.toString());
        }

        JsonObjectRequest request = new JsonObjectRequest(method, url, body,
                response -> {
                    Log.d(TAG, "Response from " + url + ": " + response.toString());
                    listener.onResponse(response);
                }, 
                error -> {
                    Log.e(TAG, "Error from " + url, error);
                    if (error.networkResponse != null) {
                        try {
                            String errorBody = new String(error.networkResponse.data, "UTF-8");
                            Log.e(TAG, "Error response body: " + errorBody);
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
                if (body != null) {
                    headers.put("Content-Type", "application/json; charset=utf-8");
                }
                
                // Get and validate token
                String token = new UserPreferences(context).getJwtToken();
                if (token == null || token.isEmpty()) {
                    Log.e(TAG, "No authentication token found");
                    throw new AuthFailureError("No authentication token found");
                }
                
                // Add token to headers
                headers.put("Cookie", "user_auth_token=" + token);
                Log.d(TAG, "Request headers: " + headers);
                
                return headers;
            }
        };

        // Set retry policy
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000, // 10 seconds timeout
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        return request;
    }


    /**
     * Tạo project mới
     */
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
                    BASE_URL + "/projects",
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
            ApiConfig.getInstance(ctx).addToRequestQueue(req);
            Log.d(TAG, "Request added to queue");
        } catch (JSONException e) {
            Log.e(TAG, "Error creating project request", e);
            errorListener.onErrorResponse(new VolleyError("Error creating request: " + e.getMessage()));
        }
    }


    /**
     * Lấy danh sách tất cả project
     */
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
        ApiConfig.getInstance(ctx).addToRequestQueue(req);
    }

    public static void getJoinedProjects(Context ctx,
                                      Response.Listener<JSONObject> listener,
                                      Response.ErrorListener errorListener) {
        JsonObjectRequest req = makeRequest(
                Request.Method.GET,
                "/projects/joined",
                null,
                ctx,
                listener,
                errorListener
        );
        ApiConfig.getInstance(ctx).addToRequestQueue(req);
    }

    /**
     * Lấy chi tiết project theo ID
     */
    public static void getProject(Context context, String projectId,
                                  Response.Listener<JSONObject> listener,
                                  Response.ErrorListener errorListener) {
        String url = BASE_URL + "/projects/" + projectId;

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

    /**
     * Cập nhật project
     */
    public static void updateProject(Context context,
                                     String projectId,
                                     JSONObject projectData,
                                     Response.Listener<JSONObject> listener,
                                     Response.ErrorListener errorListener) {
        try {
            JSONObject body = new JSONObject();
            body.put("id", Integer.parseInt(projectId));
            body.put("projectName", projectData.getString("projectName"));
            body.put("description", projectData.getString("description"));
            body.put("status", projectData.getString("status"));
            body.put("endDate", projectData.getString("endDate"));
            body.put("updatedAt", projectData.getString("updatedAt"));

            // Add owner information if available
            if (projectData.has("owner")) {
                body.put("owner", projectData.getJSONObject("owner"));
            }

            String url = "/projects/" + projectId;
            JsonObjectRequest req = makeRequest(
                    Request.Method.PUT,
                    url,
                    body,
                    context,
                    listener,
                    errorListener
            );
            ApiConfig.getInstance(context).addToRequestQueue(req);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating update request body", e);
            errorListener.onErrorResponse(new VolleyError("Error creating request body"));
        }
    }

    /**
     * Xóa project
     */
    public static void deleteProject(Context context,
                                     String projectId,
                                     Response.Listener<JSONObject> listener,
                                     Response.ErrorListener errorListener) {
        String url = "/projects/" + projectId;
        JsonObjectRequest req = makeRequest(
                Request.Method.DELETE,
                url,
                null,
                context,
                listener,
                error -> {
                    Log.d(TAG, ">>> delete error: " + error.toString());
                    errorListener.onErrorResponse(error);
                }
        );
        ApiConfig.getInstance(context).addToRequestQueue(req);
    }

    // Parse danh sách projects từ response
    public static List<Project> parseProjectsList(JSONObject response) throws JSONException {
        Log.d(TAG, "Parsing projects response: " + response.toString());
        
        List<Project> projects = new ArrayList<>();
        
        // Check response format
        if (!response.has("status")) {
            Log.e(TAG, "Response missing status field");
            throw new JSONException("Response missing status field");
        }
        
        String status = response.optString("status");
        if (!"success".equals(status)) {
            String message = response.optString("message", "Unknown error");
            Log.e(TAG, "Response status not success: " + message);
            throw new JSONException("Response status not success: " + message);
        }
        
        // Check data field
        if (!response.has("data")) {
            Log.e(TAG, "Response missing data field");
            throw new JSONException("Response missing data field");
        }
        
        JSONArray data = response.optJSONArray("data");
        if (data == null) {
            Log.e(TAG, "Data field is not an array");
            throw new JSONException("Data field is not an array");
        }
        
        Log.d(TAG, "Found " + data.length() + " projects in response");
        
        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject projectJson = data.getJSONObject(i);
                Log.d(TAG, "Parsing project " + i + ": " + projectJson.toString());
                
                Project project = new Project();
                project.setProjectID(projectJson.optInt("id", -1));
                project.setProjectName(projectJson.optString("projectName", ""));
                project.setProjectDescription(projectJson.optString("description", ""));
                project.setStatus(projectJson.optString("status", ""));
                
                // Log raw date strings
                String startDateStr = projectJson.optString("startDate", "");
                String endDateStr = projectJson.optString("endDate", "");
                Log.d(TAG, "Project " + project.getProjectName() + " - Raw dates: startDate=" + startDateStr + ", endDate=" + endDateStr);
                
                // Parse dates with error handling
                try {
                    Date startDate = ParseDateUtil.parseFlexibleIsoDate(startDateStr);
                    Date endDate = ParseDateUtil.parseFlexibleIsoDate(endDateStr);
                    Log.d(TAG, "Project " + project.getProjectName() + " - Parsed dates: startDate=" + startDate + ", endDate=" + endDate);
                    
                    project.setStartDate(startDate);
                    project.setDeadline(endDate);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing dates for project " + project.getProjectName(), e);
                    // Set default dates if parsing fails
                    project.setStartDate(new Date());
                    project.setDeadline(new Date());
                }
                
                // Parse other dates
                try {
                    project.setCreateAt(ParseDateUtil.parseFlexibleIsoDate(projectJson.optString("createdAt", "")));
                    project.setUpdateAt(ParseDateUtil.parseFlexibleIsoDate(projectJson.optString("updatedAt", "")));
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing other dates for project " + project.getProjectName(), e);
                }
                
                project.setBackgroundImg(projectJson.optString("backGround", ""));
                
                // Parse owner if available
                if (projectJson.has("owner")) {
                    try {
                        JSONObject owner = projectJson.getJSONObject("owner");
                        User user = new User();
                        user.setId(owner.optInt("id", -1));
                        user.setUsername(owner.optString("fullname", ""));
                        project.setUser(user);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing owner for project " + project.getProjectName(), e);
                    }
                }
                
                projects.add(project);
                Log.d(TAG, "Successfully parsed project " + project.getProjectName());
                
            } catch (Exception e) {
                Log.e(TAG, "Error parsing project at index " + i, e);
                // Continue with next project instead of failing completely
            }
        }
        
        Log.d(TAG, "Successfully parsed " + projects.size() + " projects");
        return projects;
    }

    // Parse một project từ JSON
    public static Project parseProject(JSONObject response) throws JSONException {
        Log.d(TAG, "Parsing project from response: " + response.toString());
        
        JSONObject data = response.getJSONObject("data");
        Log.d(TAG, "Project data object: " + data.toString());
        
        Project project = new Project();

        project.setProjectID(data.getInt("id"));
        project.setProjectName(data.getString("projectName"));
        project.setProjectDescription(data.getString("description"));
        project.setStatus(data.getString("status"));
        project.setBackgroundImg(data.optString("backGround", ""));
        
        // Parse dates with error handling
        try {
            String startDateStr = data.getString("startDate");
            String endDateStr = data.getString("endDate");
            Log.d(TAG, "Raw dates - startDate: " + startDateStr + ", endDate: " + endDateStr);
            
            project.setStartDate(ParseDateUtil.parseFlexibleIsoDate(startDateStr));
            project.setDeadline(ParseDateUtil.parseFlexibleIsoDate(endDateStr));
        } catch (Exception e) {
            Log.e(TAG, "Error parsing dates", e);
        }

        // Parse owner
        if (data.has("owner")) {
            JSONObject owner = data.getJSONObject("owner");
            Log.d(TAG, "Owner data: " + owner.toString());
            project.getUser().setId(owner.getInt("id"));
            project.getUser().setUsername(owner.getString("fullname"));
        }

        // Parse phases
        if (data.has("phases")) {
            JSONArray phasesArray = data.getJSONArray("phases");
            Log.d(TAG, "Found " + phasesArray.length() + " phases");
            List<Phase> phases = new ArrayList<>();
            for (int i = 0; i < phasesArray.length(); i++) {
                JSONObject phaseObj = phasesArray.getJSONObject(i);
                Phase phase = PhaseService.parsePhase(phaseObj);
                phases.add(phase);
            }
            project.setPhases(phases);
        }

        Log.d(TAG, "Successfully parsed project: " + project.toString());
        return project;
    }

    /**
     * Lấy danh sách phases của project
     */
    public static void getProjectPhases(Context context,
                                        String projectId,
                                        Response.Listener<JSONObject> listener,
                                        Response.ErrorListener errorListener) {
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                BASE_URL + "/phases/project/" + projectId,
                null,
                response -> {
                    Log.d(TAG, "Project phases response: " + response.toString());
                    listener.onResponse(response);
                },
                error -> {
                    Log.e(TAG, "Error getting project phases: " + error.toString());
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
                String token = new UserPreferences(context).getJwtToken();
                if (token != null && !token.isEmpty()) {
                    headers.put("Cookie", "user_auth_token=" + token);
                }
                return headers;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        ApiConfig.getInstance(context).addToRequestQueue(req);
    }

    /**
     * Lấy danh sách tasks của phase
     */
    public static void getPhaseTasks(Context context,
                                     String phaseId,
                                     Response.Listener<JSONObject> listener,
                                     Response.ErrorListener errorListener) {
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                BASE_URL + "/tasks/phase/" + phaseId,
                null,
                response -> {
                    Log.d(TAG, "Phase tasks response: " + response.toString());
                    listener.onResponse(response);
                },
                error -> {
                    Log.e(TAG, "Error getting phase tasks: " + error.toString());
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
                String token = new UserPreferences(context).getJwtToken();
                if (token != null && !token.isEmpty()) {
                    headers.put("Cookie", "user_auth_token=" + token);
                }
                return headers;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        ApiConfig.getInstance(context).addToRequestQueue(req);
    }

    // Parse danh sách phases từ response
    public static List<Phase> parsePhasesList(JSONObject response) throws JSONException {
        List<Phase> phases = new ArrayList<>();
        JSONArray data = response.optJSONArray("data");
        if (data != null) {
            for (int i = 0; i < data.length(); i++) {
                JSONObject phaseJson = data.getJSONObject(i);
                Phase phase = new Phase();
                phase.setPhaseID(phaseJson.optInt("id", -1));
                phase.setPhaseName(phaseJson.optString("phaseName", ""));
                phase.setDescription(phaseJson.optString("description", ""));
                phase.setStatus(phaseJson.optString("status", ""));
//                phase.setStartDate(ParseDateUtil.parseDate(phaseJson.optString("startDate", "")));
//                phase.setEndDate(ParseDateUtil.parseDate(phaseJson.optString("endDate", "")));
                phase.setOrderIndex(phaseJson.optInt("orderIndex", 0));
                phase.setCreateAt(ParseDateUtil.parseFlexibleIsoDate(phaseJson.optString("createdAt", "")));
//                phase.set(ParseDateUtil.parseDate(phaseJson.optString("updatedAt", "")));
                phases.add(phase);
            }
        }
        return phases;
    }

    // Parse danh sách tasks từ response
    public static List<Task> parseTasksList(JSONObject response) throws JSONException {
        List<Task> tasks = new ArrayList<>();
        JSONArray data = response.getJSONArray("data");
        Log.d("ProjectService", "Parsing tasks response: " + response.toString());
        for (int i = 0; i < data.length(); i++) {
            JSONObject taskJson = data.getJSONObject(i);
            Log.d("ProjectService", "Task " + i + " data: " + taskJson.toString());
            Task task = new Task();
            task.setTaskID(taskJson.optInt("id", -1));
            task.setTaskName(taskJson.optString("taskName", ""));
            task.setTaskDescription(taskJson.optString("description", ""));
            task.setStatus(taskJson.optString("status", ""));
            task.setPriority(taskJson.optString("priority", ""));
            task.setDueDate(ParseDateUtil.parseFlexibleIsoDate(taskJson.optString("dueDate", "")));
            task.setOrderIndex(taskJson.optInt("orderIndex", 0));

            // Parse assignedTo information
            if (!taskJson.isNull("assignedTo")) {
                JSONObject assignedToJson = taskJson.getJSONObject("assignedTo");
                int assignedToId = assignedToJson.optInt("id", 0);
                Log.d("ProjectService", "Task " + task.getTaskName() + " assignedToId: " + assignedToId);
                task.setAssignedTo(assignedToId);
            } else {
                Log.d("ProjectService", "Task " + task.getTaskName() + " has no assignedTo");
                task.setAssignedTo(0);
            }

            // Parse phase information
            if (!taskJson.isNull("phase")) {
                JSONObject phaseJson = taskJson.getJSONObject("phase");
                Phase phase = new Phase();
                phase.setPhaseID(phaseJson.optInt("id", -1));
                phase.setPhaseName(phaseJson.optString("phaseName", ""));
                phase.setDescription(phaseJson.optString("description", ""));
                phase.setStatus(phaseJson.optString("status", ""));
                phase.setOrderIndex(phaseJson.optInt("orderIndex", 0));

                // Parse project information in phase
                if (!phaseJson.isNull("project")) {
                    JSONObject projectJson = phaseJson.getJSONObject("project");
                    Project project = new Project();
                    project.setProjectID(projectJson.optInt("id", -1));
                    phase.setProject(project);
                }

                task.setPhase(phase);
            }

            tasks.add(task);
        }
        return tasks;
    }

    public static void acceptProjectInvitation(
            Context context,
            long notificationId,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {

        String url = BASE_URL + "/projects/invitations/" + notificationId + "/accept";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null, // Không có request body
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

    public static void rejectProjectInvitation(
            Context context,
            long notificationId,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {

        String url = BASE_URL + "/projects/invitations/" + notificationId + "/reject";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null, // Không có body
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
}