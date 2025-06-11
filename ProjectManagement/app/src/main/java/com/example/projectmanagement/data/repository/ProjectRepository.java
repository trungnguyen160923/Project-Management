// ProjectRepository.java
package com.example.projectmanagement.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.projectmanagement.api.ProjectService;
import com.example.projectmanagement.data.model.Project;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProjectRepository {
    private static final String TAG = "ProjectRepository";
    private static ProjectRepository instance;

    private final Context context;
    private final MutableLiveData<List<Project>> projectsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Project> projectLiveData  = new MutableLiveData<>();
    private final MutableLiveData<String>  messageLiveData  = new MutableLiveData<>();

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

    public LiveData<List<Project>> getAllProjects() {
        ProjectService.getAllProjects(context, response -> {
            try {
                if ("success".equals(response.optString("status"))) {
                    projectsLiveData.setValue(ProjectService.parseProjectsList(response));
                } else {
                    String err = response.optString("message", "Lấy project thất bại");
                    Log.e(TAG, err);
                    projectsLiveData.setValue(null);
                }
            } catch (Exception e) {
                Log.e(TAG, "Parsing error", e);
                projectsLiveData.setValue(null);
            }
        }, error -> {
            Log.e(TAG, "Network error", error);
            projectsLiveData.setValue(null);
        });
        return projectsLiveData;
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

    /** LiveData expose message */
    public LiveData<String> getMessageLiveData() {
        return messageLiveData;
    }
}
