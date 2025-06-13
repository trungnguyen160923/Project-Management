package com.example.projectmanagement.data.convertor;

import android.content.Context;

import com.example.projectmanagement.data.model.ProjectMember;
import com.example.projectmanagement.data.service.UserService;

import org.json.JSONObject;

public class ProjectMemberConverter {

    public static ProjectMember fromJson(Context context, JSONObject jsonObject) {
        ProjectMember member = new ProjectMember();

        // Gán các trường cơ bản
        member.setMemberID(jsonObject.optInt("id"));
        int userID = jsonObject.optInt("userId");
        member.setUserID(userID);
        UserService.getUser(context, userID, res -> {
            member.setUser(UserConvertor.fromJson(res.optJSONObject("data")));
        }, err -> {
            member.setUser(null);
        });
        member.setProjectID(jsonObject.optInt("projectId"));

        // Gán role (chuyển chuỗi sang enum, không phân biệt hoa thường)
        String roleString = jsonObject.optString("role", "MEMBER").toUpperCase();
        try {
            member.setRole(ProjectMember.Role.valueOf(roleString));
        } catch (IllegalArgumentException e) {
            member.setRole(ProjectMember.Role.MEMBER); // fallback
        }

        // joinAt, project, user: chưa có trong JSON → giữ nguyên null
        member.setJoinAt(null);
        member.setProject(null);

        return member;
    }
}
