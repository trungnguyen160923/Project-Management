package com.example.projectmanagement.viewmodel;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.User;
import com.example.projectmanagement.data.repository.AuthRepository;

public class LoginViewModel extends ViewModel {

    private AuthRepository authRepository;
    private MutableLiveData<UserView> userLiveData;
    private MutableLiveData<Boolean> isLoading;

    private MutableLiveData<LoginFormState>  loginFormState = new MutableLiveData<>();

    public LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    public LoginViewModel() {
        authRepository = AuthRepository.getInstance();
        userLiveData = new MutableLiveData<>();
        isLoading = new MutableLiveData<>(false);
    }

    public LiveData<UserView> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void login(String email, String password) {
        isLoading.setValue(true);
        authRepository.login(email, password).observeForever(user -> {
            if (user != null) {
                userLiveData.setValue(new UserView(user.getUsername()));
            } else {
                userLiveData.setValue(null);
            }
            isLoading.setValue(false);
        });
    }

    public void loginDataChanged(String username, String password) {
        // Giả sử username và password có thể null, đảm bảo luôn lấy chuỗi đã trim
        String emailStr = username != null ? username.trim() : "";
        String passwordStr = password != null ? password.trim() : "";

        Integer usernameError = null;
        Integer passwordError = null;
        boolean isValid = true;

        // Kiểm tra username
        if (emailStr.isEmpty()) {
            usernameError = R.string.empty_email;
            isValid = false;
        } else if (!isUserNameValid(emailStr)) {
            usernameError = R.string.invalid_email;
            isValid = false;
        }

        // Kiểm tra password
        if (passwordStr.isEmpty()) {
            passwordError = R.string.empty_password;
            isValid = false;
        } else if (!isPasswordValid(passwordStr)) {
            passwordError = R.string.invalid_password;
            isValid = false;
        }

        // Cập nhật loginFormState với thông tin lỗi và trạng thái hợp lệ
        loginFormState.setValue(new LoginFormState(usernameError, passwordError, isValid));
    }


    // Kiểm tra định dạng email sử dụng Patterns từ Android SDK
    private boolean isUserNameValid(String username) {
        return username != null && Patterns.EMAIL_ADDRESS.matcher(username).matches();
    }

    // Kiểm tra mật khẩu có đủ độ dài (trong ví dụ này là >=6 ký tự)
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() >= 6;
    }

    // Lớp đại diện trạng thái form đăng nhập
    public static class LoginFormState {
        private Integer usernameError;
        private Integer passwordError;
        private boolean isDataValid;

        public LoginFormState(Integer usernameError, Integer passwordError) {
            this.usernameError = usernameError;
            this.passwordError = passwordError;
            this.isDataValid = false;
        }

        public LoginFormState(boolean isDataValid) {
            this.usernameError = null;
            this.passwordError = null;
            this.isDataValid = isDataValid;
        }

        public LoginFormState(Integer usernameError, Integer passwordError, boolean isDataValid) {
            this.usernameError = usernameError;
            this.passwordError = passwordError;
            this.isDataValid = isDataValid;
        }

        public Integer getUsernameError() {
            return usernameError;
        }

        public Integer getPasswordError() {
            return passwordError;
        }

        public boolean isDataValid() {
            return isDataValid;
        }

        public void setPasswordError(Integer passwordError) {
            this.passwordError = passwordError;
        }

        public void setUsernameError(Integer usernameError) {
            this.usernameError = usernameError;
        }

        public void setDataValid(boolean dataValid) {
            isDataValid = dataValid;
        }
    }

    // Lớp đại diện kết quả đăng nhập
    public static class LoginResult {
        private UserView success;
        private Integer error;

        public LoginResult(UserView success) {
            this.success = success;
            this.error = null;
        }

        public LoginResult(Integer error) {
            this.error = error;
            this.success = null;
        }

        public UserView getSuccess() {
            return success;
        }

        public Integer getError() {
            return error;
        }
    }
}
