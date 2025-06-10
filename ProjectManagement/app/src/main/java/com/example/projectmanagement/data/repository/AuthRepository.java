package com.example.projectmanagement.data.repository;

import android.content.Context;
import android.util.Log;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.example.projectmanagement.api.AuthService;
import com.example.projectmanagement.data.model.User;

import org.json.JSONObject;

public class AuthRepository {

    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String errorMsg);
    }

    private static AuthRepository instance;
    public static AuthRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AuthRepository(context.getApplicationContext());
        }
        return instance;
    }

    private final Context context;

    public AuthRepository(Context context) {
        this.context = context.getApplicationContext();
    }


    // Interface callback cho đăng nhập Google (trả về JSONObject)
    public interface AuthJsonCallback {
        void onSuccess(JSONObject response);
        void onError(VolleyError error);
    }

    // Đăng nhập bằng email, password
    public void login(String email, String password, AuthCallback callback) {
        AuthService.login(context, email, password, response -> {
            try {
                Log.d("AuthRepository", "Response: " + response); // Đã là JSONObject

                String status = response.optString("status", "error");

                if ("success".equals(status)) {
                    // Đăng nhập thành công, lấy thông tin user từ "data"
                    JSONObject data = response.optJSONObject("data");
                    if (data != null) {
                        User user = new User();
                        user.setId(data.optInt("id", -1));
                        user.setUsername(data.optString("name", "")); // API trả về "name"
                        user.setEmail(data.optString("email", ""));
                        callback.onSuccess(user);
                    } else {
                        callback.onError("Dữ liệu trả về không hợp lệ (thiếu data)");
                    }
                } else {
                    // Lấy message lỗi từ "error" hoặc "message"
                    String errorMsg = response.optString("error",
                            response.optString("message", "Đăng nhập thất bại!"));
                    callback.onError(errorMsg);
                }
            } catch (Exception e) {
                e.printStackTrace();
                callback.onError("Lỗi phân tích dữ liệu: " + e.getMessage());
            }
        }, error -> {
            // Lấy thông tin lỗi từ Volley
            String errorMessage = "Lỗi không xác định";
            if (error.networkResponse != null && error.networkResponse.data != null) {
                try {
                    String errorBody = new String(error.networkResponse.data, "UTF-8");
                    errorMessage = "Lỗi: " + errorBody;
                } catch (Exception ignored) {}
            } else if (error.getMessage() != null) {
                errorMessage = error.getMessage();
            }
            callback.onError(errorMessage);
        });
    }


    // Đăng ký
    public void register(String name, String email, String password, AuthCallback callback) {
        AuthService.register(context, name, email, password, response -> {
            try {
                JSONObject obj = new JSONObject(response);
                User user = new User();
                user.setId(obj.optInt("id"));
                user.setUsername(obj.optString("username"));
                user.setEmail(obj.optString("email"));
                callback.onSuccess(user);
            } catch (Exception e) {
                callback.onError("Lỗi khi phân tích dữ liệu");
            }
        }, error -> {
            callback.onError("Đăng ký thất bại");
        });
    }

    // Đăng nhập Google (nếu cần)
    public interface AuthGoogleCallback {
        void onSuccess(User user);
        void onError(String errorMsg);
    }
    public void loginWithGoogle(String googleIdToken, AuthGoogleCallback callback) {
        AuthService.loginWithGoogle(context, googleIdToken, response -> {
            try {
                User user = new User();
                user.setId(response.optInt("id"));
                user.setUsername(response.optString("username"));
                user.setEmail(response.optString("email"));
                callback.onSuccess(user);
            } catch (Exception e) {
                callback.onError("Lỗi khi phân tích dữ liệu");
            }
        }, error -> callback.onError("Đăng nhập Google thất bại"));
    }
}
