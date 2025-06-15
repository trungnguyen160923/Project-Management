package com.example.projectmanagement.data.repository;

import android.content.Context;

import com.android.volley.VolleyError;
import com.example.projectmanagement.data.model.ProjectMember;
import com.example.projectmanagement.data.service.NotificationService;
import com.example.projectmanagement.data.service.ProjectMemberService;
import com.example.projectmanagement.utils.CustomCallback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MemberRepository {
    private Context context;

    public MemberRepository(Context context) {
        this.context = context;
    }

    public void fetchMembers(int projectId, CustomCallback<JSONObject, VolleyError> callback) {
        ProjectMemberService.fetchProjectMembers(context, projectId, callback::onSuccess, callback::onError);
    }

    public void updateMemberRole(int projectId, int userId, ProjectMember.Role role, CustomCallback<JSONObject, VolleyError> callback) {
        ProjectMemberService.changeRole(
            context,
            projectId,
            userId,
            role.toString(),
            callback::onSuccess,
            callback::onError
        );
    }

    public void removeMember(int projectId, int userId, CustomCallback<JSONObject, VolleyError> callback) {
        ProjectMemberService.removeMember(
            context,
            projectId,
            userId,
            callback::onSuccess,
            callback::onError
        );
    }
}

