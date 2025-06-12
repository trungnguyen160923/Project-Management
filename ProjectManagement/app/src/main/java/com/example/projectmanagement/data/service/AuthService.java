package com.example.projectmanagement.data.service;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.projectmanagement.api.ApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AuthService {
    private static final String TAG = "AuthService";
    private static final String API_PREFIX = "http://10.0.2.2:8080/api";

    // Đăng nhập thường
    public static void login(Context context, String email, String password,
                             Response.Listener<JSONObject> listener,
                             Response.ErrorListener errorListener) {
        try {
            String url = API_PREFIX + "/auth/login";
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("usernameOrEmail", email);
            jsonBody.put("password", password);

            Log.d(TAG, "Login request: " + jsonBody.toString());

            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, 
                response -> {
                    Log.d(TAG, "Login response: " + response.toString());
                    listener.onResponse(response);
                }, 
                error -> {
                    Log.e(TAG, "Login error: " + error.getMessage());
                    errorListener.onErrorResponse(error);
                }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            ApiClient.getInstance(context).getRequestQueue().add(jsonRequest);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating login request", e);
            errorListener.onErrorResponse(new VolleyError("Lỗi tạo request: " + e.getMessage()));
        }
    }

    // Đăng ký tài khoản mới
    public static void register(Context context, String username, String email, String password,
                              String fullname, String birthday, String gender, 
                              String socialLinks, String bio,
                              Response.Listener<JSONObject> listener,
                              Response.ErrorListener errorListener) {
        try {
            String url = API_PREFIX + "/auth/signup";
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("username", username);
            jsonBody.put("email", email);
            jsonBody.put("password", password);
            jsonBody.put("fullname", fullname);
            jsonBody.put("birthday", birthday);
            jsonBody.put("gender", gender);
            jsonBody.put("socialLinks", socialLinks);
            jsonBody.put("bio", bio);

            Log.d(TAG, "Register request: " + jsonBody.toString());

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    Log.d(TAG, "Register response: " + response.toString());
                    listener.onResponse(response);
                },
                error -> {
                    Log.e(TAG, "Register error: " + error.getMessage());
                    errorListener.onErrorResponse(error);
                }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            ApiClient.getInstance(context).getRequestQueue().add(request);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating register request", e);
            errorListener.onErrorResponse(new VolleyError("Lỗi tạo request: " + e.getMessage()));
        }
    }

    // Đăng nhập bằng Google (token từ Google Sign-In)
    public static void loginWithGoogle(Context context, String googleIdToken,
                                       Response.Listener<JSONObject> listener,
                                       Response.ErrorListener errorListener) {
        String url = API_PREFIX + "/auth/google-login";

        try {
            JSONObject body = new JSONObject();
            body.put("id_token", googleIdToken);

            Log.d(TAG, "Google login request: " + body.toString());

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> {
                    Log.d(TAG, "Google login response: " + response.toString());
                    listener.onResponse(response);
                },
                error -> {
                    Log.e(TAG, "Google login error: " + error.getMessage());
                    errorListener.onErrorResponse(error);
                }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            ApiClient.getInstance(context).getRequestQueue().add(request);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating Google login request", e);
            errorListener.onErrorResponse(new VolleyError("Lỗi tạo request: " + e.getMessage()));
        }
    }
}
