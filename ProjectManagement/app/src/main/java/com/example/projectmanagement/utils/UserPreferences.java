package com.example.projectmanagement.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.projectmanagement.data.model.User;
import com.google.gson.Gson;

public class UserPreferences {
    private static final String TAG = "UserPreferences";
    private static final String PREF_NAME = "user_prefs";
    
    // Keys for user info
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FULLNAME = "fullname";
    private static final String KEY_AVATAR = "avatar";
    private static final String KEY_JWT_TOKEN = "jwt_token";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER = "user_data";

    private final SharedPreferences preferences;
    private final Gson gson;

    public UserPreferences(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // User info management
    public void saveUserInfo(String userId, String email, String fullname, String avatar) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_FULLNAME, fullname);
        editor.putString(KEY_AVATAR, avatar);
        editor.apply();
        Log.d(TAG, "User info saved: userId=" + userId + ", email=" + email);
    }

    public void saveUser(User user) {
        try {
            String userJson = gson.toJson(user);
            Log.d(TAG, "Saving user: " + userJson);
            preferences.edit()
                    .putString(KEY_USER, userJson)
                    .putBoolean(KEY_IS_LOGGED_IN, true)
                    .apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving user: " + e.getMessage());
        }
    }

    public User getUser() {
        try {
            String userJson = preferences.getString(KEY_USER, null);
            Log.d(TAG, "Getting user: " + userJson);
            if (userJson == null) return null;
            return gson.fromJson(userJson, User.class);
        } catch (Exception e) {
            Log.e(TAG, "Error getting user: " + e.getMessage());
            return null;
        }
    }

    public void clearUserInfo() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_FULLNAME);
        editor.remove(KEY_AVATAR);
        editor.remove(KEY_USER);
        editor.apply();
        Log.d(TAG, "User info cleared");
    }

    // Getters for user info
    public String getUserId() {
        return preferences.getString(KEY_USER_ID, "");
    }

    public String getEmail() {
        return preferences.getString(KEY_EMAIL, "");
    }

    public String getFullname() {
        return preferences.getString(KEY_FULLNAME, "");
    }

    public String getAvatar() {
        return preferences.getString(KEY_AVATAR, "");
    }

    public boolean isLoggedIn() {
        String token = getJwtToken();
        boolean loggedIn = token != null && !token.isEmpty();
        Log.d(TAG, "Checking login status: " + loggedIn);
        return loggedIn;
    }

    public void setLoggedIn(boolean isLoggedIn) {
        Log.d(TAG, "Setting logged in state: " + isLoggedIn);
        preferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
                .apply();
    }

    // JWT Token management
    public void saveJwtToken(String token) {
        if (token != null && !token.isEmpty()) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(KEY_JWT_TOKEN, token);
            editor.apply();
            Log.d(TAG, "JWT token saved");
        } else {
            Log.w(TAG, "Attempted to save null or empty JWT token");
        }
    }

    public String getJwtToken() {
        String token = preferences.getString(KEY_JWT_TOKEN, null);
        if (token != null) {
            Log.d(TAG, "Getting JWT token");
        } else {
            Log.w(TAG, "No JWT token found in preferences");
        }
        return token;
    }

    public void clearJwtToken() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_JWT_TOKEN);
        editor.apply();
        Log.d(TAG, "JWT token cleared");
    }

    // Clear all data
    public void clearAll() {
        Log.d(TAG, "Clearing all preferences");
        preferences.edit().clear().apply();
    }
} 