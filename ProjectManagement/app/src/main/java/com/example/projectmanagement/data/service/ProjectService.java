package com.example.projectmanagement.data.service;

import android.content.Context;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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
        JsonObjectRequest request = new JsonObjectRequest(method, url, body,
                listener, error -> {
                Log.d(TAG,">>> er"+error.toString());
            errorListener.onErrorResponse(error);
        }) {
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
        ApiConfig.getInstance(ctx).addToRequestQueue(req);
    }


    /** Lấy chi tiết project theo ID */
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

    /** Cập nhật project */
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

    /** Xóa project */
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
        List<Project> projects = new ArrayList<>();
        JSONArray data = response.optJSONArray("data");
        if (data != null) {
            for (int i = 0; i < data.length(); i++) {
                JSONObject projectJson = data.getJSONObject(i);
                Project project = new Project();
                project.setProjectID(projectJson.optInt("id", -1));
                project.setProjectName(projectJson.optString("projectName", ""));
                project.setProjectDescription(projectJson.optString("description", ""));
                project.setStatus(projectJson.optString("status", ""));
                project.setStartDate(ParseDateUtil.parseDate(projectJson.optString("startDate", "")));
                project.setDeadline(ParseDateUtil.parseDate(projectJson.optString("endDate", "")));
                project.setCreateAt(ParseDateUtil.parseDate(projectJson.optString("createdAt", "")));
                project.setUpdateAt(ParseDateUtil.parseDate(projectJson.optString("updatedAt", "")));
                project.setBackgroundImg(projectJson.optString("backgroundImg", ""));
                projects.add(project);
            }
        }
        return projects;
    }

    // Parse một project từ JSON
    public static Project parseProject(JSONObject response) throws JSONException {
        JSONObject data = response.getJSONObject("data");
        Project project = new Project();

        project.setProjectID(data.getInt("id"));
        project.setProjectName(data.getString("projectName"));
        project.setProjectDescription(data.getString("description"));
        project.setStatus(data.getString("status"));
        project.setStartDate(ParseDateUtil.parseDate(data.getString("startDate")));
        project.setDeadline(ParseDateUtil.parseDate(data.getString("endDate")));

        // Parse owner
        if (data.has("owner")) {
            JSONObject owner = data.getJSONObject("owner");
            project.getUser().setId(owner.getInt("id"));
            project.getUser().setUsername(owner.getString("fullname"));
        }

        // Parse phases
        if (data.has("phases")) {
            JSONArray phasesArray = data.getJSONArray("phases");
            List<Phase> phases = new ArrayList<>();
            for (int i = 0; i < phasesArray.length(); i++) {
                JSONObject phaseObj = phasesArray.getJSONObject(i);
                Phase phase = PhaseService.parsePhase(phaseObj);
                phases.add(phase);
            }
            project.setPhases(phases);
        }

        return project;
    }

    /** Lấy danh sách phases của project */
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

    /** Lấy danh sách tasks của phase */
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
                phase.setCreateAt(ParseDateUtil.parseDate(phaseJson.optString("createdAt", "")));
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
        for (int i = 0; i < data.length(); i++) {
            JSONObject taskJson = data.getJSONObject(i);
            Task task = new Task();
            task.setTaskID(taskJson.optInt("id", -1));
            task.setTaskName(taskJson.optString("taskName", ""));
            task.setTaskDescription(taskJson.optString("description", ""));
            task.setStatus(taskJson.optString("status", ""));
            task.setPriority(taskJson.optString("priority", ""));
            task.setDueDate(ParseDateUtil.parseDate(taskJson.optString("dueDate", "")));
            task.setOrderIndex(taskJson.optInt("orderIndex", 0));

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

            // Handle assignedTo which might be null
            if (!taskJson.isNull("assignedTo")) {
                JSONObject assignedToJson = taskJson.getJSONObject("assignedTo");
                // Parse assignedTo information if needed
                // For now, we'll just skip it since it's not used in the current implementation
            }

            tasks.add(task);
        }
        return tasks;
    }
}