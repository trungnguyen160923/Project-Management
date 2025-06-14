package com.example.projectmanagement.ui.project;

import static com.example.projectmanagement.ui.project.BackgroundConstants.*;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import org.json.JSONException;
import org.json.JSONObject;

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
    private static final String TAG = "UPDATEPROJECTACTIVITY";

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

        // Set initial values
        binding.projectNameTiet.setText(currentProject.getProjectName());
        binding.projectDescTiet.setText(currentProject.getProjectDescription());

        // Set initial date and time
        Date deadline = currentProject.getDeadline();
        if (deadline != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(deadline);
            
            // Format date
            String formattedDate = String.format("%02d/%02d/%d", 
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.YEAR)
            );
            binding.projectDateTietDay.setText(formattedDate);
            
            // Format time
            String formattedTime = String.format("%02d:%02d",
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE)
            );
            binding.projectDateTietHour.setText(formattedTime);
            binding.projectDateTietHour.setEnabled(true);
        }

        bgImg = currentProject.getBackgroundImg();
        applyBackgroundFromString(bgImg);

        // Initial validation
        validateInputs();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(UpdateProjectViewModel.class);
        viewModel.init(this);
        viewModel.getFormState().observe(this, state -> {
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
        // Add text change listeners
        binding.projectNameTiet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                nameTouched = true;
                viewModel.dataChanged(s.toString());
                validateInputs();
            }
        });

        binding.projectDescTiet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateInputs();
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
                    
                    validateInputs();
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
                        validateInputs();
                    } catch (Exception e) {
                        Log.e("UpdateProjectActivity", "Error parsing date: " + e.getMessage());
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            );
            timePickerDialog.show();
        });
    }

    private void validateInputs() {
        String projectName = binding.projectNameTiet.getText().toString().trim();
        String description = binding.projectDescTiet.getText().toString().trim();
        String date = binding.projectDateTietDay.getText().toString().trim();
        String time = binding.projectDateTietHour.getText().toString().trim();

        boolean isValid = false;

        // Case 1: Only project name is required
        if (!projectName.isEmpty()) {
            // If no date/time is provided, just name is enough
            if (date.isEmpty() && time.isEmpty()) {
                isValid = true;
            }
            // If date/time is provided, validate them
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
                    Log.e("UpdateProjectActivity", "Error validating date/time: " + e.getMessage());
                    isValid = false;
                }
            }
        }

        Log.d("UpdateProjectActivity", "validateInputs: name=" + projectName + 
            ", date=" + date + ", time=" + time + ", isValid=" + isValid);
        binding.updateProjectBtn.setEnabled(isValid);
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
            if (!binding.updateProjectBtn.isEnabled()) {
                return;
            }

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);

            Project p = currentProject;
            p.setProjectName(binding.projectNameTiet.getText().toString().trim());
            p.setProjectDescription(binding.projectDescTiet.getText().toString().trim());
            
            // Only update date/time if they are provided
            String date = binding.projectDateTietDay.getText().toString().trim();
            String time = binding.projectDateTietHour.getText().toString().trim();
            if (!date.isEmpty() && !time.isEmpty()) {
                p.setDeadline(ParseDateUtil.parseDate(date + " " + time));
            }
            
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

    private void handleUpdateSuccess(JSONObject response) {
        try {
            JSONObject data = response.getJSONObject("data");
            Project updatedProject = new Project();
            updatedProject.setProjectID(data.getInt("projectId"));
            updatedProject.setProjectName(data.getString("projectName"));
            updatedProject.setProjectDescription(data.getString("projectDescription"));
            updatedProject.setStartDate(ParseDateUtil.parseFlexibleIsoDate(data.getString("startDate")));
            updatedProject.setDeadline(ParseDateUtil.parseFlexibleIsoDate(data.getString("endDate")));
            updatedProject.setBackgroundImg(data.getString("backgroundImg"));

            // Update ProjectHolder with the updated project
            ProjectHolder.set(updatedProject);

            // Show success message and finish activity
            Toast.makeText(this, "Cập nhật project thành công", Toast.LENGTH_SHORT).show();
            finish();
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing update response", e);
            Toast.makeText(this, "Lỗi khi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
        }
    }
}
