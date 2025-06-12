package com.example.projectmanagement.data.service;

import android.content.Context;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.projectmanagement.api.ApiClient;
import com.example.projectmanagement.utils.ApiConfig;
import com.example.projectmanagement.utils.UserPreferences;
import org.json.JSONObject;
import java.util.Map;

public class UserService {
    private static final String BASE_URL = ApiConfig.BASE_URL;
    private static final String TAG = "UserService";

    // Đăng nhập
    public static void login(Context context, String email, String password,
                           Response.Listener<JSONObject> listener,
                           Response.ErrorListener errorListener) {
        try {
            JSONObject loginData = new JSONObject();
            loginData.put("email", email);
            loginData.put("password", password);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                    BASE_URL + "/auth/login", loginData, response -> {
                        try {
                            String status = response.optString("status", "error");
                            if ("success".equals(status)) {
                                JSONObject data = response.optJSONObject("data");
                                if (data != null) {
                                    // Lưu thông tin user
                                    String userId = data.optString("id", "");
                                    String fullname = data.optString("fullname", "");
                                    String avatar = data.optString("avatar", "");
                                    
                                    UserPreferences userPreferences = new UserPreferences(context);
                                    userPreferences.saveUserInfo(userId, email, fullname, avatar);
                                    Log.d(TAG, "User info saved successfully");
                                }
                            }
                            listener.onResponse(response);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing login response", e);
                            errorListener.onErrorResponse(new VolleyError("Lỗi xử lý response đăng nhập"));
                        }
                    }, error -> {
                        if (error instanceof AuthFailureError) {
                            Log.e(TAG, "Authentication error: " + error.getMessage());
                            errorListener.onErrorResponse(new VolleyError("Email hoặc mật khẩu không đúng"));
                        } else {
                            Log.e(TAG, "Login error: " + error.getMessage());
                            errorListener.onErrorResponse(error);
                        }
                    }) {
                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    // Lưu cookie từ response headers
                    Map<String, String> headers = response.headers;
                    String cookie = headers.get("Set-Cookie");
                    if (cookie != null && !cookie.isEmpty()) {
                        Log.d(TAG, "Received cookie from server: " + cookie);
                        // Lưu cookie vào preferences
                        UserPreferences userPreferences = new UserPreferences(context);
                        userPreferences.saveJwtToken(cookie);
                    } else {
                        Log.w(TAG, "No cookie received from server");
                    }
                    return super.parseNetworkResponse(response);
                }
            };

            ApiClient.getInstance(context).addToRequestQueue(request);
        } catch (Exception e) {
            Log.e(TAG, "Error creating login request", e);
            errorListener.onErrorResponse(new VolleyError("Lỗi tạo request đăng nhập"));
        }
    }

    // Đăng ký
    public static void register(Context context, String email, String password, String fullname,
                              Response.Listener<JSONObject> listener,
                              Response.ErrorListener errorListener) {
        try {
            JSONObject registerData = new JSONObject();
            registerData.put("email", email);
            registerData.put("password", password);
            registerData.put("fullname", fullname);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                    BASE_URL + "/auth/register", registerData, response -> {
                        try {
                            String status = response.optString("status", "error");
                            if ("success".equals(status)) {
                                JSONObject data = response.optJSONObject("data");
                                if (data != null) {
                                    // Lưu thông tin user
                                    String userId = data.optString("id", "");
                                    String avatar = data.optString("avatar", "");
                                    
                                    UserPreferences userPreferences = new UserPreferences(context);
                                    userPreferences.saveUserInfo(userId, email, fullname, avatar);
                                    Log.d(TAG, "User info saved successfully after registration");
                                }
                            }
                            listener.onResponse(response);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing register response", e);
                            errorListener.onErrorResponse(new VolleyError("Lỗi xử lý response đăng ký"));
                        }
                    }, error -> {
                        if (error instanceof AuthFailureError) {
                            Log.e(TAG, "Authentication error: " + error.getMessage());
                            errorListener.onErrorResponse(new VolleyError("Email đã tồn tại"));
                        } else {
                            Log.e(TAG, "Register error: " + error.getMessage());
                            errorListener.onErrorResponse(error);
                        }
                    }) {
                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    // Lưu cookie từ response headers
                    Map<String, String> headers = response.headers;
                    String cookie = headers.get("Set-Cookie");
                    if (cookie != null && !cookie.isEmpty()) {
                        Log.d(TAG, "Received cookie from server: " + cookie);
                        // Lưu cookie vào preferences
                        UserPreferences userPreferences = new UserPreferences(context);
                        userPreferences.saveJwtToken(cookie);
                    } else {
                        Log.w(TAG, "No cookie received from server");
                    }
                    return super.parseNetworkResponse(response);
                }
            };

            ApiClient.getInstance(context).addToRequestQueue(request);
        } catch (Exception e) {
            Log.e(TAG, "Error creating register request", e);
            errorListener.onErrorResponse(new VolleyError("Lỗi tạo request đăng ký"));
        }
    }

    // Đăng xuất
    public static void logout(Context context,
                            Response.Listener<JSONObject> listener,
                            Response.ErrorListener errorListener) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                BASE_URL + "/auth/logout", null, response -> {
                    // Xóa cookie và thông tin user
                    UserPreferences userPreferences = new UserPreferences(context);
                    userPreferences.clearJwtToken();
                    userPreferences.clearUserInfo();
                    Log.d(TAG, "Logged out successfully");
                    listener.onResponse(response);
                }, error -> {
                    Log.e(TAG, "Logout error: " + error.getMessage());
                    errorListener.onErrorResponse(error);
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return ApiClient.getInstance(context).getHeaders();
            }
        };
        ApiClient.getInstance(context).addToRequestQueue(request);
    }

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
    public static void updateUserProfile(Context context, JSONObject userData,
                                       Response.Listener<JSONObject> listener,
                                       Response.ErrorListener errorListener) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT,
                BASE_URL + "/users/profile", userData, listener, error -> {
                    if (error instanceof AuthFailureError) {
                        Log.e(TAG, "Authentication error: " + error.getMessage());
                        errorListener.onErrorResponse(new VolleyError("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."));
                    } else {
                        Log.e(TAG, "Error updating user profile: " + error.getMessage());
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
}
