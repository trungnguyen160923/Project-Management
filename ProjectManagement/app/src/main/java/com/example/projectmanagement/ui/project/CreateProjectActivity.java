package com.example.projectmanagement.ui.project;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.databinding.ActivityCreateProjectBinding;
import com.example.projectmanagement.ui.main.HomeActivity;
import com.example.projectmanagement.utils.LoadingDialog;
import com.example.projectmanagement.utils.ParseDateUtil;
import com.example.projectmanagement.ui.project.vm.CreateProjectViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.example.projectmanagement.ui.project.BackgroundConstants.*;

public class CreateProjectActivity extends AppCompatActivity {
    private ActivityCreateProjectBinding binding;
    private CreateProjectViewModel viewModel;
    private LoadingDialog loadingDialog;
    private ActivityResultLauncher<Intent> bgLauncher;

    private long selectedDateMillis;
    private int selectedHour, selectedMinute;
    private String bgImg = "COLOR;#0C90F1";
    private boolean nameFieldTouched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateProjectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingDialog = new LoadingDialog(this);
        setUpBackBtn();
        setupViewModel();
        setupBackgroundPicker();
        setupDateTimePickers();
        setupCreateFlow();
    }

    private void setUpBackBtn(){
        binding.btnCloseCreateProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(CreateProjectViewModel.class);

        viewModel.getCreateFormState().observe(this, state -> {
            binding.createProjectBtn.setEnabled(state.isValid());
            if (state.getNameErrorRes() != null) {
                binding.projectNameTIL.setError(getString(state.getNameErrorRes()));
            } else {
                binding.projectNameTIL.setError(null);
            }
        });

        binding.projectNameTiet.addTextChangedListener(new SimpleWatcher() {
            @Override public void afterTextChanged(Editable s) {

                nameFieldTouched = true;
                viewModel.dataChanged(s.toString());
            }
        });
        binding.projectNameTiet.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !nameFieldTouched) {
                // Lần đầu họ chưa gõ gì mà chuyển đi: vẫn bật validate
                viewModel.dataChanged(binding.projectNameTiet.getText().toString());
                nameFieldTouched = true;
            }
        });
    }

    private void setupBackgroundPicker() {
        bgLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        applyBackground(result.getData());
                    }
                }
        );

        binding.buttonImageCustom.compositeButton.setOnClickListener(v -> {
            bgLauncher.launch(new Intent(this, BackGroundProjectActivity.class));
        });
    }

    private void applyBackground(Intent data) {
        String type = data.getStringExtra(EXTRA_TYPE);
        if (TYPE_COLOR.equals(type)) {
            int startRes = data.getIntExtra(EXTRA_START_COLOR_RES_ID, -1);
            int endRes   = data.getIntExtra(EXTRA_END_COLOR_RES_ID, -1);
            int ori      = data.getIntExtra(EXTRA_ORIENTATION_ORDINAL, 0);

            float r = 4 * getResources().getDisplayMetrics().density;
            GradientDrawable shape = new GradientDrawable();
            shape.setCornerRadius(r);

            if (endRes != -1) {
                int c1 = ContextCompat.getColor(this, startRes);
                int c2 = ContextCompat.getColor(this, endRes);
                shape.setColors(new int[]{c1, c2});
                shape.setOrientation(GradientDrawable.Orientation.values()[ori]);
                String hex1 = String.format("#%06X", 0xFFFFFF & c1);
                String hex2 = String.format("#%06X", 0xFFFFFF & c2);
                bgImg = "GRADIENT;" + hex1 + "," + hex2 + ";" + ori;
            } else {
                shape.setColor(ContextCompat.getColor(this, startRes));
                String hex = String.format("#%06X", 0xFFFFFF & ContextCompat.getColor(this, startRes));
                bgImg = "COLOR;" + hex;
            }
            binding.buttonImageCustom.buttonIcon.setImageDrawable(shape);
        } else if (TYPE_IMG.equals(type)) {
            if (data.hasExtra(EXTRA_IMG_RES)) {
                int imgRes = data.getIntExtra(EXTRA_IMG_RES, -1);
                if (imgRes != -1) {
                    binding.buttonImageCustom.buttonIcon.setImageResource(imgRes);
                    bgImg = "RESOURCE;" + imgRes;
                    return;
                }
            }
            Uri uri = data.getParcelableExtra(EXTRA_IMG_URI);
            if (uri != null) {
                com.bumptech.glide.Glide.with(this).load(uri).into(binding.buttonImageCustom.buttonIcon);
                bgImg = "URI;" + uri.toString();
            }
        }
    }

    private void setupDateTimePickers() {
        selectedDateMillis = MaterialDatePicker.todayInUtcMilliseconds();
        Calendar now = Calendar.getInstance();
        selectedHour = now.get(Calendar.HOUR_OF_DAY);
        selectedMinute = now.get(Calendar.MINUTE);

        binding.projectDateTietDay.setFocusable(false);
        binding.projectDateTietDay.setClickable(true);
        binding.projectDateTietHour.setFocusable(false);
        binding.projectDateTietHour.setClickable(true);

        binding.projectDateTietDay.setOnClickListener(v -> openDatePicker());
        binding.projectDateTietHour.setOnClickListener(v -> openTimePicker());
    }

    private void openDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker
                .Builder.datePicker()
                .setTitleText("Chọn ngày")
                .setSelection(selectedDateMillis)
                .build();
        picker.show(getSupportFragmentManager(), "DATE");
        picker.addOnPositiveButtonClickListener(sel -> {
            selectedDateMillis = sel;
            binding.projectDateTietDay.setText(
                    android.text.format.DateFormat.format("yyyy-MM-dd", sel)
            );
        });
    }

    private void openTimePicker() {
        MaterialTimePicker tp = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setTitleText("Chọn giờ")
                .setHour(selectedHour)
                .setMinute(selectedMinute)
                .build();
        tp.show(getSupportFragmentManager(), "TIME");
        tp.addOnPositiveButtonClickListener(view -> {
            selectedHour = tp.getHour();
            selectedMinute = tp.getMinute();
            binding.projectDateTietHour.setText(
                    String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
            );
        });
    }

    private void setupCreateFlow() {
        binding.createProjectBtn.setOnClickListener(v -> {
            Project p = new Project();
            p.setProjectName(binding.projectNameTiet.getText().toString().trim());

            String desc = binding.projectDescTiet.getText().toString().trim();
            if (!desc.isEmpty()) p.setProjectDescription(desc);

            String dateStr = binding.projectDateTietDay.getText().toString().trim();
            String timeStr = binding.projectDateTietHour.getText().toString().trim();
            if (dateStr.isEmpty() && !timeStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ngày đến hạn", Toast.LENGTH_SHORT).show();
                return;
            }
            Date deadline = ParseDateUtil.parseDate(
                    dateStr + (timeStr.isEmpty() ? "" : " " + timeStr)
            );
            p.setDeadline(deadline);

            p.setBackgroundImg(bgImg);
            loadingDialog.show();
            viewModel.createProject(p);
        });

        viewModel.getProjectLiveData().observe(this, proj -> {
            loadingDialog.dismiss();
            String msg = (proj != null) ? "Tạo project thành công" : "Tạo project thất bại";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            if (proj != null) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            }
        });
    }

    private abstract static class SimpleWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
        @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
    }
}
