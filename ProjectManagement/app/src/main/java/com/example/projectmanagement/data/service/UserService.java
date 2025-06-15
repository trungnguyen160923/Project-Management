package com.example.projectmanagement.data.service;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.projectmanagement.api.ApiClient;
import com.example.projectmanagement.data.model.User;
import com.example.projectmanagement.utils.ApiConfig;
import com.example.projectmanagement.utils.Helpers;
import com.example.projectmanagement.utils.UserPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class UserService {
    private static final String BASE_URL = ApiConfig.BASE_URL;
    private static final String TAG = "UserService";

    // Lấy thông tin user profile
    public static void getUserProfile(Context context,
                                      Response.Listener<JSONObject> listener,
                                      Response.ErrorListener errorListener) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                BASE_URL + "/users/profile", null, listener, error -> {
            if (error instanceof AuthFailureError) {
                Log.e(TAG, "Authentication error: " + error.getMessage());
                errorListener.onErrorResponse(new VolleyError("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."));
            } else {
                Log.e(TAG, "Error getting user profile: " + error.getMessage());
                errorListener.onErrorResponse(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return ApiClient.getInstance(context).getHeaders();
            }
        };
        ApiClient.getInstance(context).addToRequestQueue(request);
    }

    // Cập nhật thông tin user profile
    public static void updateUserProfile(
            Context context,
            JSONObject userData,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener
    ) {
        String url = BASE_URL + "/users/update/profile";
        JsonObjectRequest request = new JsonObjectRequest(
                JsonObjectRequest.Method.PUT,
                url,
                userData,
                listener,
                error -> {
                    Log.e(TAG, "Error updating user profile: " + error.toString());
                    if (error instanceof AuthFailureError) {
                        errorListener.onErrorResponse(
                                new VolleyError("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."));
                    } else {
                        String msg = null;
                        try {
                            msg = Helpers.parseError(error);
                        } catch (JSONException | UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }
                        errorListener.onErrorResponse(new VolleyError(msg));
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return ApiClient.getInstance(context).getHeaders();
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };
        ApiClient.getInstance(context).addToRequestQueue(request);
    }

    public static void getUser(Context context, int userId,
                               Response.Listener<JSONObject> listener,
                               Response.ErrorListener errorListener) {
        String url = BASE_URL + "/users/" + userId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    listener.onResponse(response);
                },
                error -> {
                    errorListener.onErrorResponse(error);
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                UserPreferences prefs = new UserPreferences(context);
                String token = prefs.getJwtToken();
                headers.put("Cookie", "user_auth_token=" + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }

    public static void searchUsers(Context context, String keyword, Response.Listener<JSONObject> listener,
                                   Response.ErrorListener errorListener) {
        String url = BASE_URL + "/users/search?query=" + keyword;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    listener.onResponse(response);
                },
                error -> {
                    errorListener.onErrorResponse(error);
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                UserPreferences prefs = new UserPreferences(context);
                String token = prefs.getJwtToken();
                headers.put("Cookie", "user_auth_token=" + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }
}
