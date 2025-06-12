package com.example.projectmanagement.data.service;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.data.model.ProjectHolder;
import com.example.projectmanagement.data.model.User;
import com.example.projectmanagement.utils.ApiConfig;
import com.example.projectmanagement.utils.ParseDateUtil;
import com.example.projectmanagement.utils.UserPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhaseService {
    private static final String TAG = "PhaseService";
    private static final String BASE_URL = ApiConfig.BASE_URL;

    public static void getProjectPhases(Context context, int projectId,
                                      Response.Listener<JSONObject> listener,
                                      Response.ErrorListener errorListener) {
        String url = BASE_URL + "/phases/project/" + projectId;
        
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

    public static void createPhase(Context context, Phase phase,
                                 Response.Listener<JSONObject> listener,
                                 Response.ErrorListener errorListener) {
        String url = BASE_URL + "/phases";
        Log.d(TAG, "Creating phase for project ID: " + ProjectHolder.get().getProjectID());
        
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("phaseName", phase.getPhaseName());
            requestBody.put("description", phase.getDescription());
            requestBody.put("status", phase.getStatus());
//            requestBody.put("startDate", phase.getStartDate());
//            requestBody.put("endDate", phase.getEndDate());
            
            // Add project info
            UserPreferences prefs = new UserPreferences(context.getApplicationContext());
            User user = prefs.getUser();
            JSONObject projectObj = new JSONObject();
            JSONObject ownerObj = new JSONObject();
            ownerObj.put("id", user.getId());
            projectObj.put("owner",ownerObj);
            projectObj.put("id", ProjectHolder.get().getProjectID());
            requestBody.put("project", projectObj);

            Log.d(TAG, "Phase creation request body: " + requestBody.toString());

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                    response -> {
                        Log.d(TAG, "Phase creation response: " + response.toString());
                        listener.onResponse(response);
                    },
                    error -> {
                        Log.e(TAG, "Error creating phase: " + error.toString());
                        if (error.networkResponse != null) {
                            try {
                                String errorBody = new String(error.networkResponse.data, "UTF-8");
                                Log.e(TAG, "Error response body: " + errorBody);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing error response", e);
                            }
                        }
                        errorListener.onErrorResponse(error);
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    String token = new UserPreferences(context).getJwtToken();
                    if (token != null && !token.isEmpty()) {
                        headers.put("Cookie", "user_auth_token=" + token);
                    }
                    headers.put("Content-Type", "application/json");
                    Log.d(TAG, "Request headers: " + headers.toString());
                    return headers;
                }
            };

            Log.d(TAG, "Adding phase creation request to queue");
            ApiConfig.getInstance(context).addToRequestQueue(request);
            Log.d(TAG, "Phase creation request added to queue");
        } catch (JSONException e) {
            Log.e(TAG, "Error creating phase request body", e);
            errorListener.onErrorResponse(new VolleyError("Error creating request body: " + e.getMessage()));
        }
    }

    public static List<Phase> parsePhasesList(JSONObject response) throws JSONException {
        List<Phase> phases = new ArrayList<>();
        JSONArray data = response.getJSONArray("data");
        
        for (int i = 0; i < data.length(); i++) {
            JSONObject phaseObj = data.getJSONObject(i);
            Phase phase = parsePhase(phaseObj);
            phases.add(phase);
        }
        
        return phases;
    }

    public static Phase parsePhase(JSONObject response) throws JSONException {
        // Lấy đối tượng data từ root
        JSONObject phaseObj = response.getJSONObject("data");

        Phase phase = new Phase();
        phase.setPhaseID(phaseObj.getInt("id"));
        phase.setPhaseName(phaseObj.getString("phaseName"));
        phase.setDescription(phaseObj.getString("description"));
        phase.setStatus(phaseObj.getString("status"));

        // Nếu bạn muốn parse startDate/endDate (nếu không NULL)
//        if (!phaseObj.isNull("startDate")) {
//            String sd = phaseObj.getString("startDate");
//            // dùng ParseDateUtil hoặc cách nào của bạn để parse ISO-datetime
//            phase.setStartDate(ParseDateUtil.parseDateTime(sd));
//        }
//        if (!phaseObj.isNull("endDate")) {
//            String ed = phaseObj.getString("endDate");
//            phase.setEndDate(ParseDateUtil.parseDateTime(ed));
//        }

        // Lấy projectId trực tiếp
        if (phaseObj.has("projectId")) {
            phase.setProjectID(phaseObj.getInt("projectId"));
        }

        // orderIndex có thể null
        if (!phaseObj.isNull("orderIndex")) {
            phase.setOrderIndex(phaseObj.getInt("orderIndex"));
        }

        // Parse createdAt / updatedAt nếu cần
        if (!phaseObj.isNull("createdAt")) {
            phase.setCreateAt(ParseDateUtil.parseDate(phaseObj.getString("createdAt")));
        }
//        if (!phaseObj.isNull("updatedAt")) {
//            phase.set(ParseDateUtil.parseDate(phaseObj.getString("updatedAt")));
//        }

        return phase;
    }

}
