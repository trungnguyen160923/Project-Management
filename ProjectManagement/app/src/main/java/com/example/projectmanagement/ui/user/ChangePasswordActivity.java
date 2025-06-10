package com.example.projectmanagement.ui.user;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectmanagement.databinding.ActivityChangePasswordBinding;

public class ChangePasswordActivity extends AppCompatActivity {

    private ActivityChangePasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarChangePw.setNavigationOnClickListener(v -> finish());

        binding.btnChangePassword.setOnClickListener(v -> {
            boolean isValid = true;

            String oldPass = binding.edtOldPassword.getText().toString().trim();
            String newPass = binding.edtNewPassword.getText().toString().trim();
            String confirmPass = binding.edtConfirmPassword.getText().toString().trim();

            // Reset error
            binding.textInputLayoutOldPassword.setError(null);
            binding.textInputLayoutNewPassword.setError(null);
            binding.textInputLayoutConfirmPassword.setError(null);

            if (TextUtils.isEmpty(oldPass)) {
                binding.textInputLayoutOldPassword.setError("Vui lòng nhập mật khẩu cũ");
                isValid = false;
            }
            if (TextUtils.isEmpty(newPass)) {
                binding.textInputLayoutNewPassword.setError("Vui lòng nhập mật khẩu mới");
                isValid = false;
            } else if (newPass.length() < 6) {
                binding.textInputLayoutNewPassword.setError("Mật khẩu mới phải từ 6 ký tự");
                isValid = false;
            } else if (newPass.equals(oldPass)) {
                binding.textInputLayoutNewPassword.setError("Mật khẩu mới phải khác mật khẩu cũ");
                isValid = false;
            }
            if (TextUtils.isEmpty(confirmPass)) {
                binding.textInputLayoutConfirmPassword.setError("Vui lòng nhập lại mật khẩu mới");
                isValid = false;
            } else if (!confirmPass.equals(newPass)) {
                binding.textInputLayoutConfirmPassword.setError("Mật khẩu nhập lại không khớp");
                isValid = false;
            }

            if (!isValid) return;

            // TODO: Thực hiện đổi mật khẩu thực tế ở đây (gọi API, v.v.)
            Toast.makeText(this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}