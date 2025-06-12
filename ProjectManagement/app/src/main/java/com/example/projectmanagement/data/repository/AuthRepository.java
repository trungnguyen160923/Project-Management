package com.example.projectmanagement.data.repository;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;
import com.example.projectmanagement.data.service.AuthService;
import com.example.projectmanagement.data.model.User;
import com.example.projectmanagement.utils.Helpers;
import com.example.projectmanagement.utils.UserPreferences;

import org.json.JSONObject;

public class AuthRepository {

    private static final String TAG = "AuthRepository";

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
    private final UserPreferences userPreferences;

    public AuthRepository(Context context) {
        this.context = context.getApplicationContext();
        this.userPreferences = new UserPreferences(context);
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
                Log.d(TAG, "Response: " + response);

                String status = response.optString("status", "error");

                if ("success".equals(status)) {
                    JSONObject data = response.optJSONObject("data");
                    if (data != null) {
                        // Parse user data
                        JSONObject userData = data.optJSONObject("user");
                        if (userData != null) {
                            User user = new User();
                            user.setId(userData.optInt("id", -1));
                            user.setUsername(userData.optString("username", ""));
                            user.setEmail(userData.optString("email", ""));
                            user.setFullname(userData.optString("fullname", ""));
                            user.setGender(userData.optString("gender", ""));
                            user.setSocial_links(userData.optString("socialLinks", ""));
                            user.setAvatar(userData.optString("avatar", ""));
                            user.setBio(userData.optString("bio", ""));
                            user.setEmail_verified(userData.optBoolean("emailVerified", false));

                            // Lưu JWT token
                            String jwtToken = data.optString("jwt", null);
                            if (jwtToken != null && !jwtToken.isEmpty()) {
                                userPreferences.saveJwtToken(jwtToken);
                                Log.d(TAG, "JWT token saved: " + jwtToken);
                            } else {
                                Log.w(TAG, "No JWT token in response");
                            }

                            // Lưu thông tin user và trạng thái đăng nhập
                            userPreferences.saveUser(user);
                            userPreferences.setLoggedIn(true);

                            callback.onSuccess(user);
                        } else {
                            callback.onError("Dữ liệu user không hợp lệ");
                        }
                    } else {
                        callback.onError("Dữ liệu trả về không hợp lệ (thiếu data)");
                    }
                } else {
                    String errorMsg = response.optString("error",
                            response.optString("message", "Đăng nhập thất bại!"));
                    callback.onError(errorMsg);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing login response", e);
                callback.onError("Lỗi phân tích dữ liệu: " + e.getMessage());
            }
        }, error -> {
            String errorMessage = "Lỗi không xác định";
            try {
              errorMessage = Helpers.parseError(error);
            } catch (Exception e) {}
            callback.onError(errorMessage);
        });
    }

    // Đăng xuất
    public void logout() {
        userPreferences.clearAll();
    }

    // Kiểm tra trạng thái đăng nhập
    public boolean isLoggedIn() {
        return userPreferences.isLoggedIn();
    }

    // Lấy thông tin user hiện tại
    public User getCurrentUser() {
        return userPreferences.getUser();
    }

    // Lấy JWT token hiện tại
    public String getCurrentToken() {
        return userPreferences.getJwtToken();
    }

    // Đăng ký
    public void register(String username, String email, String password, String fullname, 
                        String birthday, String gender, String socialLinks, String bio,
                        AuthCallback callback) {
        AuthService.register(context, username, email, password, fullname, 
                           birthday, gender, socialLinks, bio, response -> {
            try {
                Log.d(TAG, "Register Response: " + response);

                String status = response.optString("status", "error");

                if ("success".equals(status)) {
                    JSONObject data = response.optJSONObject("data");
                    if (data != null) {
                        // Parse user data
                        JSONObject userData = data.optJSONObject("user");
                        if (userData != null) {
                            User user = new User();
                            user.setId(userData.optInt("id", -1));
                            user.setUsername(userData.optString("username", ""));
                            user.setEmail(userData.optString("email", ""));
                            user.setFullname(userData.optString("fullname", ""));
                            user.setGender(userData.optString("gender", ""));
                            user.setSocial_links(userData.optString("socialLinks", ""));
                            user.setAvatar(userData.optString("avatar", ""));
                            user.setBio(userData.optString("bio", ""));
                            user.setEmail_verified(userData.optBoolean("emailVerified", false));

                            // Lưu JWT token
                            String jwtToken = data.optString("jwt", null);
                            if (jwtToken != null && !jwtToken.isEmpty()) {
                                userPreferences.saveJwtToken(jwtToken);
                                Log.d(TAG, "JWT token saved: " + jwtToken);
                            } else {
                                Log.w(TAG, "No JWT token in response");
                            }

                            // Lưu thông tin user và trạng thái đăng nhập
                            userPreferences.saveUser(user);
                            userPreferences.setLoggedIn(true);

                            callback.onSuccess(user);
                        } else {
                            callback.onError("Dữ liệu user không hợp lệ");
                        }
                    } else {
                        callback.onError("Dữ liệu trả về không hợp lệ (thiếu data)");
                    }
                } else {
                    String errorMsg = response.optString("error",
                            response.optString("message", "Đăng ký thất bại!"));
                    callback.onError(errorMsg);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing register response", e);
                callback.onError("Lỗi phân tích dữ liệu: " + e.getMessage());
            }
        }, error -> {
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

                // Lưu token nếu có
                String token = response.optString("token", null);
                if (token != null) {
                    userPreferences.saveJwtToken(token);
                }

                // Lưu thông tin user và trạng thái đăng nhập
                userPreferences.saveUser(user);
                userPreferences.setLoggedIn(true);

                callback.onSuccess(user);
            } catch (Exception e) {
                callback.onError("Lỗi khi phân tích dữ liệu");
            }
        }, error -> callback.onError("Đăng nhập Google thất bại"));
    }
}
