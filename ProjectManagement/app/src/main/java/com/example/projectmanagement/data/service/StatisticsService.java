package com.example.projectmanagement.data.service;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.projectmanagement.data.model.Statistics;
import com.example.projectmanagement.utils.ApiConfig;
import com.example.projectmanagement.utils.UserPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsService {
    private static final String TAG = "StatisticsService";
    private static final String BASE_URL = ApiConfig.BASE_URL;

    public static void getStatistics(Context context,
                                   Response.Listener<Statistics> listener,
                                   Response.ErrorListener errorListener) {
        String url = BASE_URL + "/statistics";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if ("success".equals(response.optString("status"))) {
                            JSONObject data = response.getJSONObject("data");
                            Statistics statistics = parseStatistics(data);
                            listener.onResponse(statistics);
                        } else {
                            String error = response.optString("error");
                            errorListener.onErrorResponse(new VolleyError(error));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing statistics data", e);
                        errorListener.onErrorResponse(new VolleyError("Error parsing statistics data"));
                    }
                },
                error -> {
                    Log.e(TAG, "Error fetching statistics", error);
                    errorListener.onErrorResponse(error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                UserPreferences prefs = new UserPreferences(context);
                String token = prefs.getJwtToken();
                headers.put("Cookie", "user_auth_token=" + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        ApiConfig.getInstance(context).addToRequestQueue(request);
    }

    private static Statistics parseStatistics(JSONObject data) throws JSONException {
        Statistics statistics = new Statistics();
        statistics.setOwnedProjectsCount(data.optInt("ownedProjectsCount", 0));
        statistics.setJoinedProjectsCount(data.optInt("joinedProjectsCount", 0));
        statistics.setTotalProjectsCount(data.optInt("totalProjectsCount", 0));
        statistics.setTotalTasksCount(data.optInt("totalTasksCount", 0));
        statistics.setCompletedTasksCount(data.optInt("completedTasksCount", 0));
        statistics.setPendingTasksCount(data.optInt("pendingTasksCount", 0));
        statistics.setAssignedTasksCount(data.optInt("assignedTasksCount", 0));
        statistics.setCompletedAssignedTasksCount(data.optInt("completedAssignedTasksCount", 0));

        // Parse project member stats
        List<Statistics.ProjectMemberStat> memberStats = new ArrayList<>();
        if (!data.isNull("projectMemberStats")) {
            JSONArray memberStatsArray = data.getJSONArray("projectMemberStats");
            for (int i = 0; i < memberStatsArray.length(); i++) {
                JSONObject statJson = memberStatsArray.getJSONObject(i);
                Statistics.ProjectMemberStat stat = new Statistics.ProjectMemberStat();
                stat.setProjectId(statJson.optInt("projectId"));
                stat.setProjectName(statJson.optString("projectName"));
                stat.setMemberCount(statJson.optInt("memberCount"));
                memberStats.add(stat);
            }
        }
        statistics.setProjectMemberStats(memberStats);

        // Parse phase stats
        List<Statistics.PhaseStat> phaseStats = new ArrayList<>();
        if (!data.isNull("phaseStats")) {
            JSONArray phaseStatsArray = data.getJSONArray("phaseStats");
            for (int i = 0; i < phaseStatsArray.length(); i++) {
                JSONObject statJson = phaseStatsArray.getJSONObject(i);
                Statistics.PhaseStat stat = new Statistics.PhaseStat();
                stat.setProjectId(statJson.optInt("projectId"));
                stat.setProjectName(statJson.optString("projectName"));
                stat.setPhaseCount(statJson.optInt("phaseCount"));
                phaseStats.add(stat);
            }
        }
        statistics.setPhaseStats(phaseStats);

        // Parse project task stats
        List<Statistics.ProjectTaskStat> taskStats = new ArrayList<>();
        if (!data.isNull("projectTaskStats")) {
            JSONArray taskStatsArray = data.getJSONArray("projectTaskStats");
            for (int i = 0; i < taskStatsArray.length(); i++) {
                JSONObject statJson = taskStatsArray.getJSONObject(i);
                Statistics.ProjectTaskStat stat = new Statistics.ProjectTaskStat();
                stat.setProjectId(statJson.optInt("projectId"));
                stat.setProjectName(statJson.optString("projectName"));
                stat.setTotalAssignedTasks(statJson.optInt("totalAssignedTasks"));
                stat.setCompletedAssignedTasks(statJson.optInt("completedAssignedTasks"));
                taskStats.add(stat);
            }
        }
        statistics.setProjectTaskStats(taskStats);

        return statistics;
    }
}
