package com.example.projectmanagement.data.convertor;

import static com.example.projectmanagement.utils.ParseDateUtil.parseDate;

import com.example.projectmanagement.data.model.User;

import org.json.JSONObject;

public class UserConvertor {

    public static User fromJson(JSONObject jsonObject) {
        User user = new User();
        user.setId(jsonObject.optInt("id"));
        user.setUsername(jsonObject.optString("username", null));
        user.setEmail(jsonObject.optString("email", null));
        user.setFullname(jsonObject.optString("fullname", null));
        user.setAvatar(jsonObject.optString("avatar", null));
        user.setGender(jsonObject.optString("gender", null));
        user.setSocial_links(jsonObject.optString("socialLinks", null));
        user.setBio(jsonObject.optString("bio", null));
        user.setEmail_verified(jsonObject.optBoolean("emailVerified", false));

        // Parse birthday nếu có
        String birthdayStr = jsonObject.optString("birthday", null);
        if (birthdayStr != null && !birthdayStr.isEmpty()) {
            user.setBirthday(parseDate(birthdayStr));
        }

        // Các trường không có trong JSON, giữ nguyên null
        user.setPassword(null);
        user.setCreated_at(null);
        user.setLast_updated(null);

        return user;
    }
}
