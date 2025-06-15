// ProjectRepository.java
package com.example.projectmanagement.data.repository;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.projectmanagement.data.model.User;
import com.example.projectmanagement.data.service.ProjectService;
import com.example.projectmanagement.data.model.Project;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.projectmanagement.utils.Helpers;
import com.example.projectmanagement.utils.ParseDateUtil;
import com.example.projectmanagement.utils.UserPreferences;

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
        Log.d(TAG, "Loading projects");
        projectsLiveData.setValue(new ArrayList<>()); // Reset projects list
        messageLiveData.setValue(null); // Reset error message

        // Log token status
        String token = new UserPreferences(context).getJwtToken();
        Log.d(TAG, "Token status: " + (token != null && !token.isEmpty() ? "Valid" : "Invalid"));

        ProjectService.getAllProjects(context,
                response -> {
                    Log.d(TAG, "Raw response from getAllProjects: " + response.toString());
                    try {
                        String status = response.optString("status");
                        Log.d(TAG, "Response status: " + status);
                        
                        if ("success".equals(status)) {
                            List<Project> projects = ProjectService.parseProjectsList(response);
                            Log.d(TAG, "Parsed projects count: " + (projects != null ? projects.size() : 0));
                            
                            if (projects != null && !projects.isEmpty()) {
                                Log.d(TAG, "Loaded " + projects.size() + " owned projects");
                                Log.d(TAG, "Owned projects: " + projects.toString());
                                
                                // Get joined projects
                                ProjectService.getJoinedProjects(context, 
                                    res -> {
                                        Log.d(TAG, "Raw response from getJoinedProjects: " + res.toString());
                                        try {
                                            String joinedStatus = res.optString("status");
                                            Log.d(TAG, "Joined projects status: " + joinedStatus);
                                            
                                            if ("success".equals(joinedStatus)) {
                                                List<Project> joinedProjects = ProjectService.parseProjectsList(res);
                                                Log.d(TAG, "Parsed joined projects count: " + (joinedProjects != null ? joinedProjects.size() : 0));
                                                
                                                if (joinedProjects != null && !joinedProjects.isEmpty()) {
                                                    Log.d(TAG, "Loaded " + joinedProjects.size() + " joined projects");
                                                    Log.d(TAG, "Joined projects: " + joinedProjects.toString());
                                                    projects.addAll(joinedProjects);
                                                }
                                            }
                                        } catch (JSONException e) {
                                            Log.e(TAG, "Error parsing joined projects", e);
                                            messageLiveData.postValue("Error loading joined projects: " + e.getMessage());
                                        }
                                        Log.d(TAG, "Final projects count: " + projects.size());
                                        Log.d(TAG, "Final projects list: " + projects.toString());
                                        projectsLiveData.postValue(projects);
                                    },
                                    err -> {
                                        Log.e(TAG, "Error loading joined projects", err);
                                        if (err.networkResponse != null) {
                                            try {
                                                String errorBody = new String(err.networkResponse.data, "UTF-8");
                                                Log.e(TAG, "Error response body: " + errorBody);
                                            } catch (Exception e) {
                                                Log.e(TAG, "Error parsing error response", e);
                                            }
                                        }
                                        String errMsg = null;
                                        try {
                                            errMsg = Helpers.parseError(err);
                                        } catch (JSONException | UnsupportedEncodingException e) {
                                            Log.e(TAG, "Error parsing error message", e);
                                            errMsg = "Error parsing error message: " + e.getMessage();
                                        }
                                        messageLiveData.postValue("Error loading joined projects: " + errMsg);
                                        projectsLiveData.postValue(projects); // Still post owned projects
                                    }
                                );
                            } else {
                                Log.w(TAG, "No owned projects found");
                                projectsLiveData.postValue(new ArrayList<>());
                            }
                        } else {
                            String errorMsg = response.optString("message", "Unknown error");
                            Log.e(TAG, "Failed to get projects: " + errorMsg);
                            messageLiveData.postValue("Error loading projects: " + errorMsg);
                            projectsLiveData.postValue(new ArrayList<>());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing projects", e);
                        messageLiveData.postValue("Error loading projects: " + e.getMessage());
                        projectsLiveData.postValue(new ArrayList<>());
                    }
                },
                error -> {
                    Log.e(TAG, "Error loading projects", error);
                    if (error.networkResponse != null) {
                        try {
                            String errorBody = new String(error.networkResponse.data, "UTF-8");
                            Log.e(TAG, "Error response body: " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error response", e);
                        }
                    }
                    String errMsg = null;
                    try {
                        errMsg = Helpers.parseError(error);
                    } catch (JSONException | UnsupportedEncodingException e) {
                        Log.e(TAG, "Error parsing error message", e);
                        errMsg = "Error parsing error message: " + e.getMessage();
                    }
                    messageLiveData.postValue("Error loading projects: " + errMsg);
                    projectsLiveData.postValue(new ArrayList<>());
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

    /**
     * Tạo project và trả về LiveData cho project & message
     */
    public LiveData<Project> createProject(Project project) {
        ProjectService.createProject(context, project, response -> {
            String status = response.optString("status", "error");
            String msg = response.optString("message", null);

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
                errMsg = o.optString("message", o.optString("error", ""));
            } catch (Exception ignored) {
            }
            messageLiveData.setValue(!errMsg.isEmpty() ? errMsg : error.getMessage());
            projectLiveData.setValue(null);
        });

        return projectLiveData;
    }

    public void createProject(Project project, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        ProjectService.createProject(context, project, listener, errorListener);
    }

    public LiveData<Project> updateProject(Project project) {
        Log.d(TAG, "updateProject() called with projectId=" + project.getProjectID());
        try {
            JSONObject body = new JSONObject();
            body.put("projectName", project.getProjectName());
            body.put("description", project.getProjectDescription());
            body.put("status", project.getStatus());

            // Format dates in ISO 8601 format
            String endDate = ParseDateUtil.formatDate(project.getDeadline());
            String updatedAt = ParseDateUtil.formatDate(new Date());

            // Convert to ISO format if needed
            if (endDate.contains("/")) {
                endDate = endDate.replace("/", "-");
            }

            body.put("endDate", endDate);
            body.put("updatedAt", updatedAt);

            // Add owner information
            if (project.getUser() != null) {
                JSONObject owner = new JSONObject();
                owner.put("id", project.getUser().getId());
                owner.put("username", project.getUser().getUsername());
                owner.put("email", project.getUser().getEmail());
                owner.put("fullname", project.getUser().getFullname());
                body.put("owner", owner);
            }

            Log.d(TAG, "updateProject: request body=" + body.toString());

            ProjectService.updateProject(
                    context,
                    String.valueOf(project.getProjectID()),
                    body,
                    response -> {
                        Log.d(TAG, "updateProject: server response=" + response.toString());
                        String status = response.optString("status", "error");
                        String msg = response.optString("error", null);

                        if ("success".equals(status)) {
                            try {
                                JSONObject dataObj = response.getJSONObject("data");
                                Log.d(TAG, ">>> dt obj : " + dataObj.toString());
                                Project updated = ProjectService.parseProject(response);
                                Log.d(TAG, "updateProject: parsed updated projectId=" + updated.getProjectID());
                                projectLiveData.setValue(updated);

                                // Cập nhật list nếu cần
                                List<Project> list = projectsLiveData.getValue();
                                if (list != null) {
                                    for (int i = 0; i < list.size(); i++) {
                                        if (list.get(i).getProjectID() == updated.getProjectID()) {
                                            list.set(i, updated);
                                            Log.d(TAG, "updateProject: updated project in list at index=" + i);
                                            break;
                                        }
                                    }
                                    projectsLiveData.setValue(list);
                                }

                                messageLiveData.setValue(
                                        (msg != null && !msg.isEmpty() && !msg.equals("null")) ? msg : "Cập nhật project thành công"
                                );
                                Log.d(TAG, "updateProject: success message=" + messageLiveData.getValue());
                            } catch (JSONException e) {
                                Log.e(TAG, "updateProject: JSON parse error", e);
                                projectLiveData.setValue(null);
                                messageLiveData.setValue("Lỗi phân tích dữ liệu server");
                            }
                        } else {
                            Log.w(TAG, "updateProject: status failure=" + status + ", msg=" + msg);
                            projectLiveData.setValue(null);
                            messageLiveData.setValue(
                                    msg != null ? msg : "Không thể cập nhật project"
                            );
                        }
                    },
                    err -> {
                        String errorMessage = "Lỗi không xác định";
                        try {
                            errorMessage = Helpers.parseError(err);
                        } catch (Exception e) {
                        }
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, errorMessage);
                    });

        } catch (JSONException e) {
            Log.e(TAG, "updateProject: create JSON failed", e);
            projectLiveData.setValue(null);
            messageLiveData.setValue("Lỗi tạo JSON cho request");
        }

        return projectLiveData;
    }


    private JSONObject projectToJson(Project project) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", project.getProjectID());
        json.put("projectName", project.getProjectName());
        json.put("description", project.getProjectDescription());
        json.put("status", project.getStatus());
//        json.put("startDate",   project.getStartDate()); // ví dụ: "2025-06-12T18:16:42.541Z"
        json.put("endDate", ParseDateUtil.formatDate(project.getDeadline()));
        json.put("updatedAt", ParseDateUtil.formatDate(new Date()));
        // owner
        UserPreferences prefs = new UserPreferences(context);
        User user = prefs.getUser();
        if (user != null) {
            JSONObject owner = new JSONObject();
            owner.put("id", user.getId());
            owner.put("username", user.getUsername());
            owner.put("email", user.getEmail());
            owner.put("fullname", user.getFullname());
            // … các trường còn lại nếu backend cần …
            json.put("owner", owner);
        }
        return json;
    }


    public void deleteProject(String projectId, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        ProjectService.deleteProject(context, projectId, listener, errorListener);
    }

    public LiveData<Boolean> deleteProject(int projectId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        ProjectService.deleteProject(
                context,
                String.valueOf(projectId),
                response -> {
                    Log.d(TAG, "deleteProject: server response=" + response.toString());
                    String status = response.optString("status", "error");
                    String msg = response.optString("error", null);

                    if ("success".equals(status)) {
                        // Xóa project khỏi danh sách
                        List<Project> list = projectsLiveData.getValue();
                        if (list != null) {
                            list.removeIf(p -> p.getProjectID() == projectId);
                            projectsLiveData.setValue(list);
                        }

                        messageLiveData.setValue(
                                !msg.isEmpty() && !msg.equals("null") ? msg : "Xóa project thành công"
                        );
                        result.setValue(true);
                    } else {
                        Log.w(TAG, "deleteProject: status failure=" + status + ", msg=" + msg);
                        messageLiveData.setValue(
                                msg
                        );

                        result.setValue(false);
                    }
                },
                error -> {
                    String errorMessage = "Lỗi không xác định";
                    try {
                        errorMessage = Helpers.parseError(error);
                    } catch (Exception e) {
                    }
                    messageLiveData.setValue(errorMessage);
                    Log.e(TAG, "deleteProject error", error);
                    result.setValue(false);
                }
        );

        return result;
    }

    public interface ProjectCallback {
        void onSuccess(Project project);
        void onError(String error);
    }

    public void getProjectById(int projectId, ProjectCallback callback) {
        ProjectService.getProject(context, String.valueOf(projectId),
            response -> {
                try {
                    Project project = ProjectService.parseProject(response);
                    callback.onSuccess(project);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing project", e);
                    callback.onError("Error loading project: " + e.getMessage());
                }
            },
            error -> {
                Log.e(TAG, "Error loading project", error);
                callback.onError("Error loading project: " + error.getMessage());
            }
        );
    }

}
