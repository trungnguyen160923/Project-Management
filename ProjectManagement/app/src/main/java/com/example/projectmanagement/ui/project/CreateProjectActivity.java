package com.example.projectmanagement.ui.project;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.databinding.ActivityCreateProjectBinding;
import com.example.projectmanagement.ui.main.HomeActivity;
import com.example.projectmanagement.ui.project.vm.CreateProjectViewModel;
import com.example.projectmanagement.utils.LoadingDialog;
import com.example.projectmanagement.utils.ParseDateUtil;
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
    private boolean nameTouched = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateProjectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingDialog = new LoadingDialog(this);
        setupViewModel();
        setupBackButton();
        setupUI();
        setupCreateFlow();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(CreateProjectViewModel.class);
        viewModel.init(this);

        viewModel.getFormState().observe(this, state -> {
            binding.createProjectBtn.setEnabled(state.isValid());
            binding.projectNameTIL.setError(
                    state.getNameErrorRes() != null ?
                            getString(state.getNameErrorRes()) : null
            );
        });

        viewModel.getIsLoading().observe(this, loading -> {
            if (loading) loadingDialog.show(); else loadingDialog.dismiss();
        });

        viewModel.getServerMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBackButton() {
        binding.btnCloseCreateProject.setOnClickListener(v -> finish());
    }

    private void setupUI() {
        // Name validation
        binding.projectNameTiet.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int b,int c) {}
            @Override public void onTextChanged(CharSequence s,int st,int b,int c) {}
            @Override public void afterTextChanged(Editable s) {
                nameTouched = true;
                viewModel.dataChanged(s.toString());
            }
        });
        binding.projectNameTiet.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !nameTouched) {
                viewModel.dataChanged(binding.projectNameTiet.getText().toString());
                nameTouched = true;
            }
        });

        // Background picker
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

        // Date & Time pickers
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
                int c = ContextCompat.getColor(this, startRes);
                shape.setColor(c);
                String hex = String.format("#%06X", 0xFFFFFF & c);
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
                com.bumptech.glide.Glide.with(this)
                        .load(uri)
                        .into(binding.buttonImageCustom.buttonIcon);
                bgImg = "URI;" + uri.toString();
            }
        }
    }

    private void setupCreateFlow() {
        binding.createProjectBtn.setOnClickListener(v -> {
            // Hide keyboard
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);
            }

            Project p = new Project();
            p.setProjectName(binding.projectNameTiet.getText().toString().trim());
            p.setProjectDescription(binding.projectDescTiet.getText().toString().trim());
            p.setStartDate(new Date());

            String d = binding.projectDateTietDay.getText().toString();
            String t = binding.projectDateTietHour.getText().toString();
            Date deadline = ParseDateUtil.parseDate(
                    d + (t.isEmpty() ? "" : " " + t)
            );
            p.setDeadline(deadline);
            p.setBackgroundImg(bgImg);

            // Add error handling
            try {
                viewModel.createProject(p).observe(this, success -> {
                    if (success) {
                        startActivity(new Intent(this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to create project. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e("CreateProjectActivity", "Error creating project", e);
                Toast.makeText(this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private abstract static class SimpleWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int st, int b, int c) {}
        @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
        @Override public void afterTextChanged(Editable s) {}
    }
}
