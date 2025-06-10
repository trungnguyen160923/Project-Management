package com.example.projectmanagement.viewmodel;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.User;
import com.example.projectmanagement.data.repository.AuthRepository;

public class RegisterViewModel extends ViewModel {

    private AuthRepository authRepository;
    private MutableLiveData<UserView> userLiveData;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<RegisterFormState> registerFormState = new MutableLiveData<>();

    public RegisterViewModel() {
//        authRepository = AuthRepository.getInstance();
        authRepository = null;
        userLiveData = new MutableLiveData<>();
        isLoading = new MutableLiveData<>(false);
    }

    public LiveData<RegisterFormState> getRegisterFormState() {
        return registerFormState;
    }

    public LiveData<UserView> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Gọi hàm đăng ký thông qua AuthRepository.
     * Nếu đăng ký thành công, trả về User thông qua LiveData dưới dạng RegisteredUserView.
     * Nếu thất bại, trả về null (hoặc bạn có thể xử lý error theo cách khác).
     */
    public void register(String email, String fullname, String password, String confirmPassword) {
        isLoading.setValue(true);
//        authRepository.register(email, fullname, password).observeForever(user -> {
//            if (user != null) {
//                userLiveData.setValue(new UserView(user.getFullname()));
//            } else {
//                userLiveData.setValue(null);
//            }
//            isLoading.setValue(false);
//        });
    }

    /**
     * Kiểm tra dữ liệu đăng ký và cập nhật trạng thái form.
     * Yêu cầu:
     * - Email không rỗng và phải đúng định dạng.
     * - Họ và tên không rỗng.
     * - Mật khẩu không rỗng và có độ dài ít nhất 6 ký tự.
     * - Xác nhận mật khẩu phải khớp với mật khẩu.
     */
    public void registerDataChanged(String email, String fullname, String password, String confirmPassword) {
        String emailStr = email != null ? email.trim() : "";
        String fullnameStr = fullname != null ? fullname.trim() : "";
        String passwordStr = password != null ? password.trim() : "";
        String confirmPasswordStr = confirmPassword != null ? confirmPassword.trim() : "";

        Integer emailError = null;
        Integer fullnameError = null;
        Integer passwordError = null;
        Integer confirmPasswordError = null;
        boolean isValid = true;

        if (emailStr.isEmpty()) {
            emailError = R.string.empty_email;
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
            emailError = R.string.invalid_email;
            isValid = false;
        }
        if (fullnameStr.isEmpty()) {
            fullnameError = R.string.empty_fullname;
            isValid = false;
        }
        if (passwordStr.isEmpty()) {
            passwordError = R.string.empty_password;
            isValid = false;
        } else if (passwordStr.length() < 6) {
            passwordError = R.string.invalid_password;
            isValid = false;
        }
        if (!confirmPasswordStr.equals(passwordStr)) {
            confirmPasswordError = R.string.passwords_do_not_match;
            isValid = false;
        }

        registerFormState.setValue(new RegisterFormState(emailError, fullnameError, passwordError, confirmPasswordError, isValid));
    }

    // Lớp hiển thị thông tin người dùng sau khi đăng ký thành công dành cho UI.

    // Lớp đại diện trạng thái form đăng ký
    public static class RegisterFormState {
        private Integer emailError;
        private Integer fullnameError;
        private Integer passwordError;
        private Integer confirmPasswordError;
        private boolean isDataValid;

        public RegisterFormState(Integer emailError, Integer fullnameError, Integer passwordError, Integer confirmPasswordError, boolean isDataValid) {
            this.emailError = emailError;
            this.fullnameError = fullnameError;
            this.passwordError = passwordError;
            this.confirmPasswordError = confirmPasswordError;
            this.isDataValid = isDataValid;
        }

        public Integer getEmailError() {
            return emailError;
        }

        public Integer getFullnameError() {
            return fullnameError;
        }

        public Integer getPasswordError() {
            return passwordError;
        }

        public Integer getConfirmPasswordError() {
            return confirmPasswordError;
        }

        public boolean isDataValid() {
            return isDataValid;
        }

        public void setEmailError(Integer emailError) {
            this.emailError = emailError;
        }

        public void setFullnameError(Integer fullnameError) {
            this.fullnameError = fullnameError;
        }

        public void setPasswordError(Integer passwordError) {
            this.passwordError = passwordError;
        }

        public void setConfirmPasswordError(Integer confirmPasswordError) {
            this.confirmPasswordError = confirmPasswordError;
        }

        public void setDataValid(boolean dataValid) {
            isDataValid = dataValid;
        }
    }

    // Lớp đại diện kết quả đăng ký
    public static class RegisterResult {
        private UserView success;
        private Integer error;

        public RegisterResult(UserView success) {
            this.success = success;
            this.error = null;
        }

        public RegisterResult(Integer error) {
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
