package com.example.projectmanagement.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.projectmanagement.ui.main.HomeActivity;
import com.example.projectmanagement.R;
import com.example.projectmanagement.utils.LoadingDialog;
import com.example.projectmanagement.viewmodel.LoginViewModel;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private TextInputLayout email_login_til, password_login_til;
    private Button btnLogin, btnRegister;
    private ProgressBar progressBar;

    private LoadingDialog loadingDialog;

    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);  // Gán layout đăng nhập của bạn

        // Ánh xạ view từ layout
        etEmail = findViewById(R.id.username);
        etPassword = findViewById(R.id.password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btnRegister);
        email_login_til = findViewById(R.id.email_login_til);
        password_login_til = findViewById(R.id.password_login_til);



        // Khởi tạo ViewModel (nếu cần factory, thêm vào tham số)
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        loadingDialog = new LoadingDialog(this);

        // Quan sát trạng thái form đăng nhập để hiện thị lỗi cho từng ô
        loginViewModel.getLoginFormState().observe(this, loginFormState -> {
            if (loginFormState == null) {
                return;
            }
            // Hiển thị lỗi cho ô email nếu có
            if (loginFormState.getUsernameError() != null) {
//                etEmail.setError(getString(loginFormState.getUsernameError()));
                email_login_til.setError(getString(loginFormState.getUsernameError()));
            } else {
                email_login_til.setError(null);
            }
            // Hiển thị lỗi cho ô password nếu có
            if (loginFormState.getPasswordError() != null) {
                password_login_til.setError(getString(loginFormState.getPasswordError()));
            } else {
                password_login_til.setError(null);
            }
            // Cho phép nút đăng nhập được kích hoạt nếu input hợp lệ
            btnLogin.setEnabled(loginFormState.isDataValid());
        });

        // Quan sát kết quả đăng nhập (nếu đăng nhập thành công, trả về User, nếu thất bại trả về null hoặc error code)
        loginViewModel.getUserLiveData().observe(this, user -> {
            // Ẩn loading dialog khi có kết quả đăng nhập
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                // Đoạn code bạn muốn thực thi sau 3 giây
                loadingDialog.dismiss();
                if (user != null) {
                    // Đăng nhập thành công: hiển thị thông báo thành công và chuyển màn hình
                    Toast.makeText(getApplicationContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();
                }
            }, 3000);


        });

        loginViewModel.getLoginError().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                // Bạn có thể delay hoặc dismiss loadingDialog ở đây nếu muốn
                loadingDialog.dismiss();
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                Log.d("Looix >>>>>>>>>>>>>", message);
            }
        });

        // Quan sát trạng thái loading nếu cần hiển thị/hide ProgressBar
        loginViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                // Nếu sử dụng ProgressBar:
                // progressBar.setVisibility(View.VISIBLE);
                btnLogin.setEnabled(false);
            } else {
                // progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
            }
        });

        // TextWatcher để kiểm tra dữ liệu khi người dùng nhập
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
                // Cập nhật trạng thái form khi người dùng nhập
                loginViewModel.loginDataChanged(etEmail.getText().toString(), etPassword.getText().toString());
            }
        };

        etEmail.addTextChangedListener(afterTextChangedListener);
        etPassword.addTextChangedListener(afterTextChangedListener);

        // Xử lý sự kiện nút Đăng nhập
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            loadingDialog.show();
            // Gọi hàm login trong ViewModel
            loginViewModel.login(email, password);
        });

        // Xử lý sự kiện nút Đăng ký
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
    }
}
