package com.example.projectmanagement.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.projectmanagement.ui.main.HomeActivity;
import com.example.projectmanagement.R;
import com.example.projectmanagement.utils.LoadingDialog;
import com.example.projectmanagement.viewmodel.RegisterViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout emailTIL, fullnameTIL, passwordTIL, confirmPasswordTIL;
    private TextInputEditText etEmail, etFullname, etPassword, etConfirmPassword;
    private Button btnRegister, btnLogin;

    private RegisterViewModel registerViewModel;

    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Ánh xạ view từ layout
        emailTIL = findViewById(R.id.email_register_til);
        fullnameTIL = findViewById(R.id.fullname_register_til);
        passwordTIL = findViewById(R.id.password_register_til);
        confirmPasswordTIL = findViewById(R.id.confirmPassword_til);

        etEmail = findViewById(R.id.username);
        etFullname = findViewById(R.id.fullname);
        etPassword = findViewById(R.id.password);
        etConfirmPassword = findViewById(R.id.confirmPassword);

        btnRegister = findViewById(R.id.sign_up_page_btn);
        btnLogin = findViewById(R.id.btnLogin);

        // Khởi tạo ViewModel
        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
        loadingDialog = new LoadingDialog(this);

        // Quan sát trạng thái form đăng ký để hiển thị lỗi (cho mỗi trường)
        registerViewModel.getRegisterFormState().observe(this, registerFormState -> {
            if (registerFormState == null)
                return;
            // Hiển thị lỗi cho ô email
            if (registerFormState.getEmailError() != null) {
                emailTIL.setError(getString(registerFormState.getEmailError()));
            } else {
                emailTIL.setError(null);
            }
            // Hiển thị lỗi cho ô fullname
            if (registerFormState.getFullnameError() != null) {
                fullnameTIL.setError(getString(registerFormState.getFullnameError()));
            } else {
                fullnameTIL.setError(null);
            }
            // Hiển thị lỗi cho ô password
            if (registerFormState.getPasswordError() != null) {
                passwordTIL.setError(getString(registerFormState.getPasswordError()));
            } else {
                passwordTIL.setError(null);
            }
            // Hiển thị lỗi cho ô xác nhận mật khẩu
            if (registerFormState.getConfirmPasswordError() != null) {
                confirmPasswordTIL.setError(getString(registerFormState.getConfirmPasswordError()));
            } else {
                confirmPasswordTIL.setError(null);
            }
            // Cho phép nút đăng ký chỉ khi tất cả input hợp lệ
            btnRegister.setEnabled(registerFormState.isDataValid());
        });

        // TextWatcher để cập nhật trạng thái form khi nhập
        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần xử lý
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Không cần xử lý
            }
            @Override
            public void afterTextChanged(Editable s) {
                registerViewModel.registerDataChanged(
                        etEmail.getText().toString(),
                        etFullname.getText().toString(),
                        etPassword.getText().toString(),
                        etConfirmPassword.getText().toString());
            }
        };

        etEmail.addTextChangedListener(afterTextChangedListener);
        etFullname.addTextChangedListener(afterTextChangedListener);
        etPassword.addTextChangedListener(afterTextChangedListener);
        etConfirmPassword.addTextChangedListener(afterTextChangedListener);

        // Xử lý nút Đăng ký
        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String fullname = etFullname.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            loadingDialog.show();

            registerViewModel.register(email, fullname, password, confirmPassword);

        });

        // Xử lý nút chuyển sang màn hình Đăng nhập
        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        // Quan sát kết quả đăng ký
        registerViewModel.getUserLiveData().observe(this, user -> {
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                loadingDialog.dismiss();
                if (user != null) {
                    // Đăng ký thành công
                    Toast.makeText(getApplicationContext(), "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                    finish();
                } else {
                    // Đăng ký thất bại
                    Toast.makeText(getApplicationContext(), "Đăng ký thất bại, vui lòng kiểm tra lại thông tin", Toast.LENGTH_SHORT).show();
                }
            },3000);
        });
    }
}
