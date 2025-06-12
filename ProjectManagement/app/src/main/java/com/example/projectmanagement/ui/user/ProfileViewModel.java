package com.example.projectmanagement.ui.user;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.projectmanagement.data.service.UserService;
import com.example.projectmanagement.data.model.User;
import com.example.projectmanagement.utils.ParseDateUtil;
import com.example.projectmanagement.utils.UserPreferences;
import org.json.JSONObject;
import java.util.regex.Pattern;

public class ProfileViewModel extends ViewModel {
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isEditing = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private Context context;
    private UserPreferences userPreferences;

    public LiveData<User> getUser() { return user; }
    public LiveData<Boolean> getIsEditing() { return isEditing; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void init(Context context) {
        this.context = context;
        this.userPreferences = new UserPreferences(context);
        loadUserProfile();
    }

    private void loadUserProfile() {
        // Lấy thông tin user từ UserPreferences
        User savedUser = userPreferences.getUser();
        if (savedUser != null) {
            user.setValue(savedUser);
            Log.d("ProfileViewModel", "Loaded user from preferences: " + savedUser.getFullname());
        } else {
            errorMessage.setValue("Không tìm thấy thông tin người dùng");
        }
    }

    public void updateProfile(User updatedUser) {
        try {
            JSONObject userData = new JSONObject();
            userData.put("fullname", updatedUser.getFullname());
            userData.put("birthday", updatedUser.getBirthday());
            userData.put("gender", updatedUser.getGender());
            userData.put("socialLinks", updatedUser.getSocial_links());
            userData.put("bio", updatedUser.getBio());

            UserService.updateUserProfile(context, userData, response -> {
                try {
                    String status = response.optString("status", "error");
                    if ("success".equals(status)) {
                        JSONObject data = response.optJSONObject("data");
                        if (data != null) {
                            User user = new User();
                            user.setId(data.optInt("id", -1));
                            user.setUsername(data.optString("username", ""));
                            user.setEmail(data.optString("email", ""));
                            user.setFullname(data.optString("fullname", ""));
                            user.setBirthday(ParseDateUtil.parseDate(data.optString("birthday", "")));
                            user.setGender(data.optString("gender", ""));
                            user.setSocial_links(data.optString("socialLinks", ""));
                            user.setAvatar(data.optString("avatar", ""));
                            user.setBio(data.optString("bio", ""));
                            user.setEmail_verified(data.optBoolean("emailVerified", false));
                            
                            // Lưu user mới vào preferences
                            userPreferences.saveUser(user);
                            this.user.setValue(user);
                            errorMessage.setValue(null);
                        }
                    } else {
                        errorMessage.setValue(response.optString("error", "Không thể cập nhật thông tin"));
                    }
                } catch (Exception e) {
                    errorMessage.setValue("Lỗi: " + e.getMessage());
                }
            }, error -> {
                errorMessage.setValue("Lỗi kết nối: " + error.getMessage());
            });
        } catch (Exception e) {
            errorMessage.setValue("Lỗi: " + e.getMessage());
        }
    }

    // Để lấy trường riêng (nếu cần)
    public String getFullname()  { return user.getValue() != null ? user.getValue().getFullname() : ""; }
    public String getEmail()     { return user.getValue() != null ? user.getValue().getEmail() : ""; }
    public String getBirthday()  { return user.getValue() != null ? ParseDateUtil.formatDate(user.getValue().getBirthday()) : ""; }
    public String getGender()    { return user.getValue() != null ? user.getValue().getGender() : ""; }
    public String getSocialLink(){ return user.getValue() != null ? user.getValue().getSocial_links() : ""; }
    public String getBio()       { return user.getValue() != null ? user.getValue().getBio() : ""; }

    // Setter cho toàn bộ User hoặc từng trường
    public void setUser(User u) { user.setValue(u); }
    public void setFullname(String fullname) {
        if (user.getValue() != null) {
            User u = user.getValue();
            u.setFullname(fullname);
            user.setValue(u);
        }
    }
    public void setEmail(String email) {
        if (user.getValue() != null) {
            User u = user.getValue();
            u.setEmail(email);
            user.setValue(u);
        }
    }
    public void setBirthday(String birthday) {
        if (user.getValue() != null) {
            User u = user.getValue();
            u.setBirthday(ParseDateUtil.parseDate(birthday));
            user.setValue(u);
        }
    }
    public void setGender(String gender) {
        if (user.getValue() != null) {
            User u = user.getValue();
            u.setGender(gender);
            user.setValue(u);
        }
    }
    public void setSocialLink(String link) {
        if (user.getValue() != null) {
            User u = user.getValue();
            u.setSocial_links(link);
            user.setValue(u);
        }
    }
    public void setBio(String bio) {
        if (user.getValue() != null) {
            User u = user.getValue();
            u.setBio(bio);
            user.setValue(u);
        }
    }

    // Bật/tắt chế độ edit
    public void toggleEditing() {
        Boolean curr = isEditing.getValue() != null && isEditing.getValue();
        isEditing.setValue(!curr);
    }

    // Validate link đơn giản (https?://…)
    public boolean isLinkValid() {
        User u = user.getValue();
        if (u == null || u.getSocial_links() == null) return false;
        Pattern p = Pattern.compile("https?://[\\w./-]+");
        return p.matcher(u.getSocial_links()).matches();
    }

    public void initMockData() {
        User u = new User(
                1,
                "username",
                "user@example.com",
                null
        );
        u.setFullname("Nguyễn Văn A");
        u.setBirthday(ParseDateUtil.parseDate("01/01/2000"));
        u.setGender("Nam");
        u.setSocial_links("https://facebook.com/user");
        u.setBio("Đây là mô tả ngắn về người dùng.");
        user.setValue(u);
    }
}
