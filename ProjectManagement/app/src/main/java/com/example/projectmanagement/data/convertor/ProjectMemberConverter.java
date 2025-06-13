package com.example.projectmanagement.data.convertor;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;
import com.example.projectmanagement.data.model.ProjectMember;
import com.example.projectmanagement.data.model.User;
import com.example.projectmanagement.data.service.UserService;
import com.example.projectmanagement.utils.CustomCallback;

import org.json.JSONObject;

public class ProjectMemberConverter {
    private static final String TAG = "ProjectMemberConverter";

    public static void fromJsonWithCallback(Context context, JSONObject jsonObject,
                                                     CustomCallback<ProjectMember, VolleyError> callback) {
        ProjectMember member = new ProjectMember();

        // Gán các trường cơ bản
        member.setMemberID(jsonObject.optInt("id"));
        member.setRole(ProjectMember.Role.valueOf(jsonObject.optString("role")));
        int userID = jsonObject.optInt("userId");
        member.setUserID(userID);
        UserService.getUser(context, userID, res -> {
            User user = UserConvertor.fromJson(res.optJSONObject("data"));
            Log.d(TAG, ">>> onSuccess getUser with id: " + user);
            member.setUser(user);
            Log.d(TAG, ">>> member in get user 1234: " + member);
            callback.onSuccess(member);
        }, err -> {
            Log.d(TAG, ">>> onError getUser with id: " + err);
            member.setUser(null);
            callback.onError(err);
        });
    }
}
