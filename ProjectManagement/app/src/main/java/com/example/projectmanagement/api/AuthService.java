package com.example.projectmanagement.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AuthService {
    static String apiPrefix = "http://10.0.2.2:8080/api";

    // Đăng nhập thường
    public static void login(Context context, String email, String password,
                             Response.Listener<JSONObject> listener,
                             Response.ErrorListener errorListener) {

        String url = apiPrefix + "/auth/login";
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("usernameOrEmail", email);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            errorListener.onErrorResponse(new VolleyError("JSON tạo body bị lỗi"));
            return;
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        ApiClient.getInstance(context).getRequestQueue().add(jsonRequest);
    }

    // Đăng ký tài khoản mới
    public static void register(Context context, String name, String email, String password,
                                Response.Listener<String> listener,
                                Response.ErrorListener errorListener) {
        String url = "https://api.example.com/auth/register";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url, listener, errorListener) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };
        ApiClient.getInstance(context).getRequestQueue().add(postRequest);
    }

    // Đăng nhập bằng Google (token từ Google Sign-In)
    public static void loginWithGoogle(Context context, String googleIdToken,
                                       Response.Listener<JSONObject> listener,
                                       Response.ErrorListener errorListener) {
        String url = "https://api.example.com/auth/google-login";

        // Nếu backend nhận dạng body là JSON, dùng JsonObjectRequest:
        JSONObject body = new JSONObject();
        try {
            body.put("id_token", googleIdToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, body, listener, errorListener);

        ApiClient.getInstance(context).getRequestQueue().add(postRequest);
    }
}
