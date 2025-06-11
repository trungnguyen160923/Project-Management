package com.example.projectmanagement.ui.user;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.User;
import com.example.projectmanagement.databinding.ActivityProfileUserBinding;
import com.example.projectmanagement.utils.ParseDateUtil;

import java.util.Calendar;

public class ProfileUserActivity extends AppCompatActivity {

    private ActivityProfileUserBinding binding;
    private ProfileViewModel vm;
    private boolean isEditing = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        vm = new ViewModelProvider(this).get(ProfileViewModel.class);
        vm.init(this); // Khởi tạo với context

        // Observe user LiveData để luôn sync UI
        vm.getUser().observe(this, user -> {
            if (user != null ) {
                bindAvatar(user);
                binding.edtFullname.setText(user.getFullname());
                binding.edtBirthday.setText(ParseDateUtil.formatDate(user.getBirthday()));
                binding.edtSocialLinks.setText(user.getSocial_links());
                binding.edtBio.setText(user.getBio());
                binding.tvFullname.setText(user.getFullname());
                binding.tvUsername.setText(user.getEmail());
                // Spinner gender
                ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) binding.spinnerGender.getAdapter();
                if (adapter != null) {
                    int pos = adapter.getPosition(user.getGender());
                    if (pos >= 0) binding.spinnerGender.setSelection(pos);
                }
            }
        });

        // Observe error message
        vm.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        // Back button (dùng finish thay vì onBackPressed)
        binding.toolbarProfileUser.setNavigationOnClickListener(v -> {
            Log.d("ProfileUserActivity", "Navigation icon clicked");
            finish();
        });

        // Spinner giới tính
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                this, R.array.gender_options, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerGender.setAdapter(genderAdapter);

        // Khi thay đổi giới tính trên spinner
        binding.spinnerGender.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (isEditing) {
                    String gender = genderAdapter.getItem(position).toString();
                    User user = vm.getUser().getValue();
                    if (user != null) {
                        user.setGender(gender);
                        vm.setUser(user);
                    }
                }
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Bio có cuộn dọc
        binding.edtBio.setMovementMethod(new ScrollingMovementMethod());
        binding.edtBio.setVerticalScrollBarEnabled(true);

        // Chọn ngày sinh
        binding.edtBirthday.setOnClickListener(v -> {
            if (!isEditing) return;
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (DatePicker view, int y, int m, int d) -> {
                String dateStr = String.format("%02d/%02d/%04d", d, m + 1, y);
                binding.edtBirthday.setText(dateStr);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Nút cập nhật/lưu
        binding.btnUpdate.setOnClickListener(v -> {
            if (!isEditing) {
                setEditMode(true);
            } else {
                boolean isValid = true;

                // Validate tên
                String fullname = binding.edtFullname.getText().toString().trim();
                if (fullname.isEmpty()) {
                    binding.textInputLayoutFullname.setError("Tên không được để trống");
                    isValid = false;
                } else {
                    binding.textInputLayoutFullname.setError(null);
                }

                // Validate ngày sinh
                String birthdayStr = binding.edtBirthday.getText().toString().trim();
                if (!birthdayStr.isEmpty()) {
                    Calendar selected = ParseDateUtil.parseDateToCalendar(birthdayStr);
                    Calendar now = Calendar.getInstance();
                    if (selected != null && selected.after(now)) {
                        binding.textInputLayoutBirthday.setError("Ngày sinh không hợp lệ");
                        isValid = false;
                    } else {
                        binding.textInputLayoutBirthday.setError(null);
                    }
                } else {
                    binding.textInputLayoutBirthday.setError("Vui lòng chọn ngày sinh");
                    isValid = false;
                }

                // Validate social link
                String social = binding.edtSocialLinks.getText().toString().trim();
                if (!social.isEmpty() && !social.matches("https?://[\\w./-]+")) {
                    binding.textInputLayoutSocialLinks.setError("Link không hợp lệ");
                    isValid = false;
                } else {
                    binding.textInputLayoutSocialLinks.setError(null);
                }

                if (!isValid) return;

                // Update User
                User user = vm.getUser().getValue();
                if (user != null) {
                    user.setFullname(fullname);
                    user.setBirthday(ParseDateUtil.parseDate(birthdayStr));
                    user.setGender(binding.spinnerGender.getSelectedItem().toString());
                    user.setSocial_links(social);
                    user.setBio(binding.edtBio.getText().toString());
                    vm.updateProfile(user); // Gọi API cập nhật
                }
                setEditMode(false);
            }
        });

        // Nhấn "đây" đổi mật khẩu
        binding.tvUpdatePassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        setEditMode(false);
    }

    private void setEditMode(boolean enable) {
        isEditing = enable;
        binding.edtFullname.setEnabled(enable);
        // binding.edtEmail.setEnabled(false); // Email luôn disable
        binding.edtBirthday.setEnabled(enable);
        binding.spinnerGender.setEnabled(enable);
        binding.edtSocialLinks.setEnabled(enable);
        binding.edtBio.setEnabled(enable);
        binding.btnUpdate.setText(enable ? "Lưu" : "Cập nhật thông tin");
    }

    // AvatarView custom: avatar là url hay null đều chuẩn
    private void bindAvatar(User user) {
        String avatar = user.getAvatar();
        if (avatar != null && !avatar.isEmpty()) {
            binding.avatarView.setImage(Uri.parse(avatar));
        } else {
            binding.avatarView.setName(user.getFullname());
        }
    }
}
