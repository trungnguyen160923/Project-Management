// ProjectRepository.java
package com.example.projectmanagement.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.projectmanagement.data.service.ProjectService;
import com.example.projectmanagement.data.model.Project;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.android.volley.Response;
import com.android.volley.VolleyError;

public class ProjectRepository {
    private static final String TAG = "ProjectRepository";
    private static ProjectRepository instance;

    private final Context context;
    private final MutableLiveData<List<Project>> projectsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Project> projectLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> messageLiveData = new MutableLiveData<>();

    private ProjectRepository(Context ctx) {
        this.context = ctx.getApplicationContext();
        projectsLiveData.setValue(new ArrayList<>());
    }

    public static ProjectRepository getInstance(Context ctx) {
        if (instance == null) {
            instance = new ProjectRepository(ctx);
        }
        return instance;
    }

    public LiveData<List<Project>> getProjects() {
        loadProjects();
        return projectsLiveData;
    }

    public LiveData<Project> getProject(int projectId) {
        loadProject(projectId);
        return projectLiveData;
    }

    public LiveData<String> getMessage() {
        return messageLiveData;
    }

    private void loadProjects() {
        ProjectService.getAllProjects(context,
            response -> {
                try {
                    List<Project> projects = ProjectService.parseProjectsList(response);
                    projectsLiveData.postValue(projects);
                    Log.d(TAG, "Projects loaded successfully: " + projects.size());
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing projects", e);
                    messageLiveData.postValue("Error loading projects: " + e.getMessage());
                }
            },
            error -> {
                Log.e(TAG, "Error loading projects", error);
                messageLiveData.postValue("Error loading projects: " + error.getMessage());
            }
        );
    }

    private void loadProject(int projectId) {
        Log.d(TAG, "Loading project with ID: " + projectId);
        ProjectService.getProject(context, String.valueOf(projectId),
            response -> {
                try {
                    Project project = ProjectService.parseProject(response);
                    projectLiveData.postValue(project);
                    Log.d(TAG, "Project loaded successfully: " + project.getProjectName());
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing project", e);
                    messageLiveData.postValue("Error loading project: " + e.getMessage());
                }
            },
            error -> {
                Log.e(TAG, "Error loading project", error);
                messageLiveData.postValue("Error loading project: " + error.getMessage());
            }
        );
    }

    /** Tạo project và trả về LiveData cho project & message */
    public LiveData<Project> createProject(Project project) {
        ProjectService.createProject(context, project, response -> {
            String status = response.optString("status", "error");
            String msg    = response.optString("message", null);

            if ("success".equals(status)) {
                try {
                    Project created = ProjectService.parseProject(response);
                    projectLiveData.setValue(created);

                    // cập nhật list nếu cần
                    List<Project> list = projectsLiveData.getValue();
                    if (list == null) list = new ArrayList<>();
                    list.add(created);
                    projectsLiveData.setValue(list);

                    messageLiveData.setValue(msg != null ? msg : "Tạo project thành công");
                } catch (JSONException e) {
                    Log.e(TAG, "Parse created project", e);
                    projectLiveData.setValue(null);
                    messageLiveData.setValue("Lỗi phân tích dữ liệu server");
                }
            } else {
                projectLiveData.setValue(null);
                messageLiveData.setValue(msg != null ? msg : "Không thể tạo project");
            }
        }, error -> {
            // cố gắng parse body JSON để lấy message
            String errMsg = "";
            try {
                String body = new String(error.networkResponse.data, "UTF-8");
                JSONObject o = new JSONObject(body);
                errMsg       = o.optString("message", o.optString("error", ""));
            } catch (Exception ignored) {}
            messageLiveData.setValue(!errMsg.isEmpty() ? errMsg : error.getMessage());
            projectLiveData.setValue(null);
        });

        return projectLiveData;
    }

    public void createProject(Project project, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        ProjectService.createProject(context, project, listener, errorListener);
    }

    public void updateProject(String projectId, JSONObject projectData, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        ProjectService.updateProject(context, projectId, projectData, listener, errorListener);
    }

    public void deleteProject(String projectId, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        ProjectService.deleteProject(context, projectId, listener, errorListener);
    }
}
