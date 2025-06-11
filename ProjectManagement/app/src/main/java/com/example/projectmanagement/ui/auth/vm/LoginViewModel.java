package com.example.projectmanagement.ui.auth.vm;

import android.app.Application;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.User;
import com.example.projectmanagement.data.repository.AuthRepository;

public class LoginViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<UserView> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private final MutableLiveData<String> loginError = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        authRepository = AuthRepository.getInstance(application);
    }

    public LiveData<UserView> getUserLiveData() { return userLiveData; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<LoginFormState> getLoginFormState() { return loginFormState; }
    public LiveData<String> getLoginError() { return loginError; }

    public void login(String email, String password) {
        isLoading.setValue(true);
        authRepository.login(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                userLiveData.setValue(new UserView(user.getUsername()));
                loginError.setValue(null);
                isLoading.setValue(false);
            }
            @Override
            public void onError(String errorMsg) {
                userLiveData.setValue(null);
                loginError.setValue(errorMsg != null ? errorMsg : "Đăng nhập thất bại");
                isLoading.setValue(false);
            }
        });
    }

    public void loginDataChanged(String username, String password) {
        String emailStr = username != null ? username.trim() : "";
        String passwordStr = password != null ? password.trim() : "";

        Integer usernameError = null;
        Integer passwordError = null;
        boolean isValid = true;

        if (emailStr.isEmpty()) {
            usernameError = R.string.empty_email;
            isValid = false;
        } else if (!isUserNameValid(emailStr)) {
            usernameError = R.string.invalid_email;
            isValid = false;
        }
        if (passwordStr.isEmpty()) {
            passwordError = R.string.empty_password;
            isValid = false;
        } else if (!isPasswordValid(passwordStr)) {
            passwordError = R.string.invalid_password;
            isValid = false;
        }
        loginFormState.setValue(new LoginFormState(usernameError, passwordError, isValid));
    }

    private boolean isUserNameValid(String username) {
        return username != null && Patterns.EMAIL_ADDRESS.matcher(username).matches();
    }
    private boolean isPasswordValid(String password) {
        return password != null && password.length() >= 6;
    }

    // UserView class (chỉ để bind UI, không cần nhiều trường)
    public static class UserView {
        private final String displayName;
        public UserView(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    // Trạng thái form đăng nhập
    public static class LoginFormState {
        private final Integer usernameError;
        private final Integer passwordError;
        private final boolean isDataValid;
        public LoginFormState(Integer usernameError, Integer passwordError, boolean isDataValid) {
            this.usernameError = usernameError;
            this.passwordError = passwordError;
            this.isDataValid = isDataValid;
        }
        public Integer getUsernameError() { return usernameError; }
        public Integer getPasswordError() { return passwordError; }
        public boolean isDataValid() { return isDataValid; }
    }

    public boolean isLoggedIn() {
        return authRepository.isLoggedIn();
    }

    public User getCurrentUser() {
        return authRepository.getCurrentUser();
    }

    public void logout() {
        authRepository.logout();
    }
}
