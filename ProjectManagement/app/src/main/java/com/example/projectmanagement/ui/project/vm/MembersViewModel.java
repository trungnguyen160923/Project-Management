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

public class MembersViewModel extends ViewModel {
    private String TAG = "InviteMemberViewModel";
    private final MutableLiveData<List<ProjectMember>> memberListLive = new MutableLiveData<>();
    private MemberRepository memberRepository;
    private Context context;
    private Project savedProject;

    public MembersViewModel() {
    }

    public void init(Context context) {
        this.context = context.getApplicationContext();
        memberRepository = new MemberRepository(context);
        fetchMembers();
    }

    private void fetchMembers() {
        Log.d(TAG, "Fetching members for project: " + savedProject.getProjectID());
        memberRepository.fetchMembers(savedProject.getProjectID(), new CustomCallback<JSONObject, VolleyError>() {
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
                                Log.d(TAG, ">>> Added member: " + result.getUser().getEmail() + 
                                    ", Role: " + result.getRole());
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
                                Log.e(TAG, ">>> Error converting member: " + errMsg);
                                Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, ">>> Error parsing members: " + e.getMessage());
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
                Log.e(TAG, ">>> onError fetchMembers: " + errMsg);
            }
        });
    }

    public void updateMemberRole(ProjectMember member, ProjectMember.Role newRole) {
        memberRepository.updateMemberRole(savedProject.getProjectID(), member.getUser().getId(), newRole, new CustomCallback<JSONObject, VolleyError>() {
            @Override
            public void onSuccess(JSONObject result) {
                // Refresh member list after successful update
                fetchMembers();
                Toast.makeText(context, "Cập nhật quyền thành công", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(VolleyError volleyError) {
                String errMsg = "Không thể cập nhật quyền thành viên";
                try {
                    errMsg = Helpers.parseError(volleyError);
                } catch (Exception e) {
                }
                Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void removeMember(ProjectMember member) {
        memberRepository.removeMember(savedProject.getProjectID(), member.getUser().getId(), new CustomCallback<JSONObject, VolleyError>() {
            @Override
            public void onSuccess(JSONObject result) {
                // Refresh member list after successful removal
                fetchMembers();
                Toast.makeText(context, "Đã xóa thành viên khỏi dự án", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(VolleyError volleyError) {
                String errMsg = "Không thể xóa thành viên";
                try {
                    errMsg = Helpers.parseError(volleyError);
                } catch (Exception e) {
                }
                Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public LiveData<List<ProjectMember>> getMemberListLive() {
        return memberListLive;
    }

    public void setProjectData(Project project) {
        savedProject = project;
    }
}
