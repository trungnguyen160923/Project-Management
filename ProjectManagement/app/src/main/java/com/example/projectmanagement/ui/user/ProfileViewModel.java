package com.example.projectmanagement.ui.user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.regex.Pattern;

public class ProfileViewModel extends ViewModel {
    private final MutableLiveData<Boolean> isEditing = new MutableLiveData<>(false);
    // LiveData cho từng field
    private final MutableLiveData<String> email = new MutableLiveData<>();
    private final MutableLiveData<String> birthday = new MutableLiveData<>();
    private final MutableLiveData<String> gender = new MutableLiveData<>();
    private final MutableLiveData<String> socialLink = new MutableLiveData<>();
    private final MutableLiveData<String> bio = new MutableLiveData<>();

    // getter
    public LiveData<Boolean> getIsEditing() { return isEditing; }
    public LiveData<String> getEmail() { return email; }
    public LiveData<String> getBirthday() { return birthday; }
    public LiveData<String> getGender() { return gender; }
    public LiveData<String> getSocialLink() { return socialLink; }
    public LiveData<String> getBio() { return bio; }

    // setter
    public void setEmail(String e) { email.setValue(e); }
    public void setBirthday(String b) { birthday.setValue(b); }
    public void setGender(String g) { gender.setValue(g); }
    public void setSocialLink(String s) { socialLink.setValue(s); }
    public void setBio(String b) { bio.setValue(b); }

    // Bật/tắt chế độ edit
    public void toggleEditing() {
        Boolean curr = isEditing.getValue() != null && isEditing.getValue();
        isEditing.setValue(!curr);
    }

    // Validate link đơn giản (https?://…)
    public boolean isLinkValid() {
        String url = socialLink.getValue();
        if (url == null) return false;
        Pattern p = Pattern.compile("https?://[\\w./-]+");
        return p.matcher(url).matches();
    }
}

