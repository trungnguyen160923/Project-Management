package com.example.projectmanagement.ui.project;

import static com.example.projectmanagement.ui.project.BackgroundConstants.*;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.data.model.ProjectHolder;
import com.example.projectmanagement.databinding.ActivityUpdateProjectBinding;
import com.example.projectmanagement.ui.project.vm.UpdateProjectViewModel;
import com.example.projectmanagement.utils.LoadingDialog;
import com.example.projectmanagement.utils.ParseDateUtil;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UpdateProjectActivity extends AppCompatActivity {
    private ActivityUpdateProjectBinding binding;
    private UpdateProjectViewModel viewModel;
    private LoadingDialog loadingDialog;
    private ActivityResultLauncher<Intent> bgLauncher;
    private Project currentProject;
    private long selectedDateMillis;
    private int selectedHour, selectedMinute;
    private String bgImg;
    private boolean nameTouched = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateProjectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingDialog = new LoadingDialog(this);
        initProject();
        setupViewModel();
        setupBackButton();
        setupUI();
        setupUpdateFlow();
    }

    private void initProject() {
        currentProject = ProjectHolder.get();
        if (currentProject == null) {
            Toast.makeText(this, "Không tìm thấy project", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        binding.projectNameTiet.setText(currentProject.getProjectName());
        binding.projectDescTiet.setText(currentProject.getProjectDescription());

        Date start = currentProject.getStartDate();
        if (start != null) {
            selectedDateMillis = start.getTime();
            binding.projectDateTietDay.setText(
                    android.text.format.DateFormat.format("yyyy-MM-dd", start)
            );
        }

        Date deadline = currentProject.getDeadline();
        if (deadline != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(deadline);
            selectedHour = cal.get(Calendar.HOUR_OF_DAY);
            selectedMinute = cal.get(Calendar.MINUTE);
            binding.projectDateTietHour.setText(
                    String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
            );
        }

        bgImg = currentProject.getBackgroundImg();
        applyBackgroundFromString(bgImg);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(UpdateProjectViewModel.class);
        viewModel.init(this);
        viewModel.getFormState().observe(this, state -> {
            binding.updateProjectBtn.setEnabled(state.isValid());
            binding.projectNameTIL.setError(
                    state.getNameErrorRes() != null ? getString(state.getNameErrorRes()) : null
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
        binding.btnCloseUpdateProject.setOnClickListener(v -> finish());
    }

    private void setupUI() {
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
        bgLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        applyBackground(result.getData());
                    }
                }
        );
        binding.projectDateTietDay.setFocusable(false);
        binding.projectDateTietDay.setClickable(true);
        binding.projectDateTietHour.setFocusable(false);
        binding.projectDateTietHour.setClickable(true);
        binding.projectDateTietDay.setOnClickListener(v -> openDatePicker());
        binding.projectDateTietHour.setOnClickListener(v -> openTimePicker());
    }

    private void openDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
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

    private void applyBackgroundFromString(String bg) {
        float r = 4 * getResources().getDisplayMetrics().density;
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(r);
        // parse bgImg string and set shape
        binding.getRoot().setBackground(shape);
    }

    private void applyBackground(Intent data) {
        String type = data.getStringExtra(EXTRA_TYPE);
        if (TYPE_COLOR.equals(type)) {
            int startRes = data.getIntExtra(EXTRA_START_COLOR_RES_ID, -1);
            int endRes   = data.getIntExtra(EXTRA_END_COLOR_RES_ID, -1);
            int ori      = data.getIntExtra(EXTRA_ORIENTATION_ORDINAL, 0);
            float r = 4 * getResources().getDisplayMetrics().density;
            GradientDrawable shape = new GradientDrawable(); shape.setCornerRadius(r);
            if (endRes != -1) {
                int c1 = ContextCompat.getColor(this, startRes);
                int c2 = ContextCompat.getColor(this, endRes);
                shape.setColors(new int[]{c1, c2});
                shape.setOrientation(GradientDrawable.Orientation.values()[ori]);
                bgImg = "GRADIENT;" + String.format("#%06X", 0xFFFFFF & c1)
                        + "," + String.format("#%06X", 0xFFFFFF & c2)
                        + ";" + ori;
            } else {
                int c = ContextCompat.getColor(this, startRes);
                shape.setColor(c);
                bgImg = "COLOR;" + String.format("#%06X", 0xFFFFFF & c);
            }
            binding.getRoot().setBackground(shape);
        }
    }

    private void setupUpdateFlow() {
        binding.updateProjectBtn.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);
            Project p = currentProject;
            p.setProjectName(binding.projectNameTiet.getText().toString().trim());
            p.setProjectDescription(binding.projectDescTiet.getText().toString().trim());
            p.setStartDate(new Date(selectedDateMillis));
            p.setDeadline(ParseDateUtil.parseDate(
                    binding.projectDateTietDay.getText().toString() + " " +
                            binding.projectDateTietHour.getText().toString()
            ));
            p.setBackgroundImg(bgImg);
            viewModel.updateProject(p).observe(this, success -> {
                if (success) {
                    ProjectHolder.set(p);
                    finish();
                } else {
                    Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
