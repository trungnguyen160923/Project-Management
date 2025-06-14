package com.example.projectmanagement.ui.project.vm;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.VolleyError;
import com.example.projectmanagement.data.convertor.ProjectMemberConverter;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.data.model.ProjectMember;
import com.example.projectmanagement.data.repository.MemberRepository;
import com.example.projectmanagement.utils.CustomCallback;
import com.example.projectmanagement.utils.Helpers;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MenuProjectViewModel extends ViewModel {
    private String TAG = "MenuProjectViewModel";
    private final MutableLiveData<Project> projectLive = new MutableLiveData<>();
    private final MutableLiveData<List<ProjectMember>> memberListLive = new MutableLiveData<>();
    private MemberRepository memberRepository;
    private Context context;

    public MenuProjectViewModel() {
    }

    public void init(Context context) {
        this.context = context.getApplicationContext();
        memberRepository = new MemberRepository(context);
        fetchMembers();
    }

    private void fetchMembers() {
        Project project = projectLive.getValue();
        if (project == null) {
            Log.e(TAG, "Project is null, cannot fetch members");
            return;
        }

        memberRepository.fetchMembers(project.getProjectID(), new CustomCallback<JSONObject, VolleyError>() {
            @Override
            public void onSuccess(JSONObject result) {
                Log.d(TAG, ">>> onSuccess fetchMembers: " + result.toString());
                List<ProjectMember> members = new ArrayList<>();
                JSONArray jsonArray = result.optJSONArray("data");
                Log.d(TAG, ">>> json arr: " + jsonArray.toString());
                try {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject memberJson = jsonArray.getJSONObject(i);
                        Log.d(TAG,">>> memberJson: " + memberJson.toString());
                        ProjectMemberConverter.fromJsonWithCallback(context, memberJson, new CustomCallback<ProjectMember, VolleyError>() {
                            @Override
                            public void onSuccess(ProjectMember result) {
                                members.add(result);
                                memberListLive.setValue(members);
                            }

                            @Override
                            public void onError(VolleyError volleyError) {
                                String errMsg = "Không thể lấy dữ liệu thành viên";
                                try {
                                    errMsg = Helpers.parseError(volleyError);
                                } catch (Exception e) {
                                }
                                Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VolleyError volleyError) {
                String errMsg = "Lỗi không thể lấy danh sách các thành viên";
                try {
                    errMsg = Helpers.parseError(volleyError);
                } catch (Exception e) {
                }
                Log.d(TAG, ">>> onError fetchMembers: " + errMsg);
            }
        });
    }

    public LiveData<Project> getProjectLive() {
        return projectLive;
    }

    public LiveData<List<ProjectMember>> getMemberListLive() {
        return memberListLive;
    }

    public void setProject(Project project) {
        projectLive.setValue(project);
        if (context != null) {
            fetchMembers();
        }
    }
}

