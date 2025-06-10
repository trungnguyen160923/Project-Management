package com.example.projectmanagement.ui.user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.projectmanagement.data.model.User;
import com.example.projectmanagement.utils.ParseDateUtil;

import java.util.regex.Pattern;

public class ProfileViewModel extends ViewModel {
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isEditing = new MutableLiveData<>(false);

    public LiveData<User> getUser() { return user; }
    public LiveData<Boolean> getIsEditing() { return isEditing; }

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
