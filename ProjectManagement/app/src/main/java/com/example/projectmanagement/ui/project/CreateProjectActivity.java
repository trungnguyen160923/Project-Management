package com.example.projectmanagement.ui.project;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

        // Set default time
        binding.projectDateTietHour.setText("00:00");
        binding.projectDateTietHour.setEnabled(false); // Disable time picker initially

        // Set up date picker
        binding.projectDateTietDay.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    
                    // Check if selected date is in the past
                    Calendar today = Calendar.getInstance();
                    today.set(Calendar.HOUR_OF_DAY, 0);
                    today.set(Calendar.MINUTE, 0);
                    today.set(Calendar.SECOND, 0);
                    today.set(Calendar.MILLISECOND, 0);
                    
                    if (selectedDate.before(today)) {
                        Toast.makeText(this, "Không thể chọn ngày trong quá khứ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    String formattedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);
                    binding.projectDateTietDay.setText(formattedDate);
                    
                    // Enable time picker when date is selected
                    binding.projectDateTietHour.setEnabled(true);
                    
                    // If selected date is today, set minimum time to current time
                    if (selectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        selectedDate.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                        selectedDate.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
                        // Set current time as minimum
                        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                        int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);
                        binding.projectDateTietHour.setText(String.format("%02d:%02d", currentHour, currentMinute));
                    } else {
                        // Reset time to 00:00 for future dates
                        binding.projectDateTietHour.setText("00:00");
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            
            // Set minimum date to today
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });

        // Set up time picker
        binding.projectDateTietHour.setOnClickListener(v -> {
            if (binding.projectDateTietDay.getText().toString().isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ngày trước", Toast.LENGTH_SHORT).show();
                return;
            }

            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    // Check if selected date is today
                    String selectedDate = binding.projectDateTietDay.getText().toString();
                    Calendar today = Calendar.getInstance();
                    Calendar selectedDateCal = Calendar.getInstance();
                    
                    try {
                        String[] dateParts = selectedDate.split("/");
                        selectedDateCal.set(
                            Integer.parseInt(dateParts[2]), // year
                            Integer.parseInt(dateParts[1]) - 1, // month (0-based)
                            Integer.parseInt(dateParts[0]) // day
                        );
                        
                        if (selectedDateCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                            selectedDateCal.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                            selectedDateCal.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
                            
                            // If today, check if selected time is in the past
                            Calendar selectedTime = Calendar.getInstance();
                            selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            selectedTime.set(Calendar.MINUTE, minute);
                            
                            if (selectedTime.before(today)) {
                                Toast.makeText(this, "Không thể chọn thời gian trong quá khứ", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        
                        String formattedTime = String.format("%02d:%02d", hourOfDay, minute);
                        binding.projectDateTietHour.setText(formattedTime);
                    } catch (Exception e) {
                        Log.e("CreateProjectActivity", "Error parsing date: " + e.getMessage());
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            );
            timePickerDialog.show();
        });

        // Add text change listeners to validate input
        binding.projectNameTiet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateInputs();
            }
        });

        binding.projectDateTietDay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateInputs();
            }
        });

        binding.projectDateTietHour.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateInputs();
            }
        });

        // Initially disable create button
        binding.createProjectBtn.setEnabled(false);
    }

    private void validateInputs() {
        String projectName = binding.projectNameTiet.getText().toString().trim();
        String date = binding.projectDateTietDay.getText().toString().trim();
        String time = binding.projectDateTietHour.getText().toString().trim();

        boolean isValid = false;

        // Case 1: Only project name is required
        if (!projectName.isEmpty() && date.isEmpty() && time.isEmpty()) {
            isValid = true;
        }
        // Case 2: Both date and time are provided and valid
        else if (!date.isEmpty() && !time.isEmpty()) {
            try {
                // Parse date
                String[] dateParts = date.split("/");
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(
                    Integer.parseInt(dateParts[2]), // year
                    Integer.parseInt(dateParts[1]) - 1, // month (0-based)
                    Integer.parseInt(dateParts[0]) // day
                );

                // Parse time
                String[] timeParts = time.split(":");
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);

                // Check if date is in the past
                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);

                if (selectedDate.before(today)) {
                    isValid = false;
                } else if (selectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                         selectedDate.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                         selectedDate.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
                    // If today, check if time is in the past
                    Calendar selectedTime = Calendar.getInstance();
                    selectedTime.set(Calendar.HOUR_OF_DAY, hour);
                    selectedTime.set(Calendar.MINUTE, minute);
                    isValid = !selectedTime.before(today);
                } else {
                    isValid = true;
                }
            } catch (Exception e) {
                Log.e("CreateProjectActivity", "Error validating date/time: " + e.getMessage());
                isValid = false;
            }
        }

        binding.createProjectBtn.setEnabled(isValid);
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
            if (!binding.createProjectBtn.isEnabled()) {
                return;
            }
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
            Log.d("CreateProjectActivity", "Date: " + d + ", Time: " + t);
            
            // Format date and time to match supported pattern
            String dateTimeStr = d + " " + t;
            Log.d("CreateProjectActivity", "Formatted date time: " + dateTimeStr);
            
            Date deadline = ParseDateUtil.parseDate(dateTimeStr);
            Log.d("CreateProjectActivity", "Parsed deadline: " + deadline);
            
            if (deadline == null) {
                Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
                return;
            }
            
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
