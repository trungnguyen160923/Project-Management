package com.example.projectmanagement.ui.task;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectmanagement.R;
import com.example.projectmanagement.data.convertor.CommentConvertor;
import com.example.projectmanagement.data.convertor.FileConvertor;
import com.example.projectmanagement.data.model.Comment;
import com.example.projectmanagement.data.model.File;
import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.data.model.ProjectHolder;
import com.example.projectmanagement.data.model.Task;
import com.example.projectmanagement.data.model.User;
import com.example.projectmanagement.data.service.CommentService;
import com.example.projectmanagement.data.service.ProjectMemberService;
import com.example.projectmanagement.data.service.FileService;
import com.example.projectmanagement.data.service.TaskService;
import com.example.projectmanagement.databinding.ActivityTaskBinding;
import com.example.projectmanagement.ui.adapter.FileAttachmentAdapter;
import com.example.projectmanagement.ui.adapter.ImageAttachmentAdapter;
import com.example.projectmanagement.ui.adapter.TaskMemberAdapter;
import com.example.projectmanagement.ui.task.vm.TaskViewModel;
import com.example.projectmanagement.utils.EnumFileType;
import com.example.projectmanagement.utils.Helpers;
import com.example.projectmanagement.utils.UserPreferences;
import com.example.projectmanagement.viewmodel.AvatarView;
import com.example.projectmanagement.data.model.ProjectMemberHolder;

import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TaskActivity extends AppCompatActivity {
    private String TAG = "TaskActivity";
    private ActivityTaskBinding binding;
    private TaskViewModel viewModel;
    private InputMethodManager imm;
    private ImageAttachmentAdapter imageAdapter;
    private FileAttachmentAdapter fileAdapter;
    private ActivityResultLauncher<String[]> imagePickerLauncher;
    private ActivityResultLauncher<String[]> filePickerLauncher;
    private TaskMemberAdapter adapter;
    private UserPreferences userPreferences;
    private String fullComment = "";

    private Task savedTask;
    private boolean isTaskModified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        viewModel.setContext(this);

        userPreferences = new UserPreferences(this);

        // Get task from intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Task task = extras.getParcelable("task");
            Log.d(TAG, ">>> task when changing activity: " + task);
            savedTask = task;
            if (task != null) {
                // Fetch task details from API
                viewModel.fetchTaskDetail(task);

                // Fetch project members
                viewModel.fetchProjectMembers();

                // Observe messages for feedback
                viewModel.getMessage().observe(this, message -> {
                    if (message != null && !message.isEmpty()) {
                        showToast(message);
                    }
                });
            }
        }

        // Restore task modification state if activity was recreated
        if (savedInstanceState != null) {
            isTaskModified = savedInstanceState.getBoolean("isTaskModified", false);
        }

        initEdgeToEdge();
        setupToolbar();
        initAdapters();
        registerPickers();
        setupListeners();
        setupObservers();
        setupCommentBar();

        hideImageSection();
        hideFileSection();
        hideCmtSection();

        viewModel.toggleImagesExpanded();
        viewModel.toggleFilesExpanded();

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(binding.etDescription, InputMethodManager.SHOW_IMPLICIT);

        // Listener for checkbox
        binding.checkboxCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateCardStrokeColor(isChecked);
            viewModel.markTaskAsComplete(isChecked);
            isTaskModified = true;
        });

        binding.etDescription.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.confirmBar.setVisibility(View.VISIBLE);
                binding.commentBar.setVisibility(View.GONE);
                imm.showSoftInput(binding.etDescription, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        View.OnClickListener hideAndReset = v -> {
            binding.etDescription.clearFocus();
            imm.hideSoftInputFromWindow(binding.etDescription.getWindowToken(), 0);
            binding.confirmBar.setVisibility(View.GONE);
            binding.commentBar.setVisibility(View.VISIBLE);
        };

        binding.btnCancelDes.setOnClickListener(hideAndReset);
        binding.btnConfirmDes.setOnClickListener(v -> {
            Log.d(TAG, "Confirm button clicked");
            String text = binding.etDescription.getText().toString().trim();
            Task currentTask = viewModel.getTask().getValue();
            if (currentTask != null) {
                showToast("Đang cập nhật mô tả...");
                // Gọi API cập nhật mô tả
                TaskService.updateTask(
                        this,
                        currentTask.getTaskID(),
                        ProjectHolder.get().getProjectID(),
                        text,
                        null, // Giữ nguyên tiêu đề
                        response -> {
                            try {
                                if ("success".equals(response.optString("status"))) {
                                    // Cập nhật UI ngay lập tức
                                    currentTask.setTaskDescription(text);
                                    viewModel.updateTask(currentTask);
                                    showToast("Đã cập nhật mô tả");
                                    hideAndReset.onClick(v);
                                } else {
                                    String error = response.optString("error");
                                    showToast("Lỗi: " + error);
                                }
                            } catch (Exception e) {
                                showToast("Lỗi xử lý dữ liệu");
                                Log.e(TAG, "Error updating task description", e);
                            }
                        },
                        error -> {
                            String errMsg = "Không thể cập nhật mô tả";
                            try {
                                errMsg = Helpers.parseError(error);
                            } catch (Exception ignored) {
                            }
                            showToast(errMsg);
                            Log.e(TAG, "Error updating task description", error);
                        }
                );
            }
        });

        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            binding.getRoot().getWindowVisibleDisplayFrame(r);
            int screenHeight = binding.getRoot().getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;
            int barH = binding.confirmBar.getHeight();

            if (keypadHeight > screenHeight * 0.15) {
                binding.confirmBar.setY(r.bottom - barH);
            } else {
                binding.confirmBar.setY(screenHeight - barH);
            }
        });

        CommentService.fetchCommentsByTaskId(this, savedTask.getTaskID(), res -> {
            JSONArray data = res.optJSONArray("data");
            if (data != null) {
                try {
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject jsonObject = data.getJSONObject(i);
                        Comment comment = CommentConvertor.fromJson(jsonObject);
                        viewModel.addComment(comment, comment.getUserID());
                    }
                } catch (Exception e) {
                }
            }
        }, err -> {
            String errMsg = "Không thể lấy danh sách các bình luận";
            try {
                errMsg = Helpers.parseError(err);
            } catch (Exception e) {
            }
            Toast.makeText(this, errMsg, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isTaskModified", isTaskModified);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called, isTaskModified: " + isTaskModified);
        if (isTaskModified) {
            setResult(RESULT_OK);
            Log.d(TAG, "Setting result to RESULT_OK in onPause");
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed called, isTaskModified: " + isTaskModified);
        if (isTaskModified) {
            setResult(RESULT_OK);
            Log.d(TAG, "Setting result to RESULT_OK in onBackPressed");
        }
        super.onBackPressed();
    }

    private void setupObservers() {
        viewModel.getIsImagesExpanded().observe(this, isExpanded -> {
            if (isExpanded) {
                showImageSection();
            } else {
                hideImageSection();
            }
        });

        viewModel.getIsFilesExpanded().observe(this, isExpanded -> {
            if (isExpanded) {
                showFileSection();
            } else {
                hideFileSection();
            }
        });

        viewModel.getIsCommentsExpanded().observe(this, isExpanded -> {
            if (isExpanded) {
                showAllComments();
            } else {
                hideCmtSection();
            }
        });

        viewModel.getComments().observe(this, comments -> {
            if (comments != null) {
                showAllComments();
            }
        });

        viewModel.getTaskDescription().observe(this, description -> {
            if (!description.equals("null")) {
                binding.etDescription.setText(description);
            }
        });

        // Observe task data
        viewModel.getTask().observe(this, task -> {
            if (task != null) {
                // Set task name in toolbar
                binding.toolbarTask.setTitle(task.getTaskName());

                // Set due date
                if (task.getDueDate() != null) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    binding.tvDueDate.setText(dateFormat.format(task.getDueDate()));
                } else {
                    binding.tvDueDate.setText("Chưa có ngày hết hạn");
                }

                // Set status - chỉ set một lần khi task được load
                if (task.getStatus() != null && !task.getStatus().isEmpty()) {
                    boolean isDone = "DONE".equalsIgnoreCase(task.getStatus());
                    binding.checkboxCompleted.setChecked(isDone);
                    updateCardStrokeColor(isDone);
                }

                // Check if project members are loaded
                List<User> members = viewModel.getProjectMembers().getValue();
                if (members != null && !members.isEmpty()) {
                    // Both task and members are loaded, update UI
                    initMemberUI();
                }
            }
        });

        viewModel.getProjectMembers().observe(this, members -> {
            if (members != null && !members.isEmpty()) {
                // Check if task is loaded
                Task task = viewModel.getTask().getValue();
                if (task != null) {
                    // Both task and members are loaded, update UI
                    initMemberUI();
                }
            }
        });

        viewModel.getAllProjectPhases().observe(this, phases -> {
            // Update phase list in move task dialog if it's open
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View focused = getCurrentFocus();
            if (focused instanceof EditText) {
                Rect outRect = new Rect();
                focused.getGlobalVisibleRect(outRect);

                // Kiểm tra xem click có nằm trong vùng của confirm bar không
                Rect confirmBarRect = new Rect();
                binding.confirmBar.getGlobalVisibleRect(confirmBarRect);
                if (confirmBarRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    return super.dispatchTouchEvent(ev);
                }

                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    focused.clearFocus();
                    imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
                    binding.confirmBar.setVisibility(View.GONE);
                    binding.commentBar.setVisibility(View.VISIBLE);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void showAllComments() {
        List<Comment> commentList = viewModel.getComments().getValue();
        if (commentList == null || commentList.isEmpty()) return;

        binding.commentContainer.setVisibility(View.VISIBLE);
        binding.commentContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);
        SimpleDateFormat fmt = new SimpleDateFormat("d MMM yyyy 'lúc' HH:mm", Locale.getDefault());

        for (Comment c : commentList) {
            View item = inflater.inflate(R.layout.item_comment, binding.commentContainer, false);
            AvatarView avatar = item.findViewById(R.id.ivCommentAvatar);
            TextView tvName = item.findViewById(R.id.tvCommentName);
            TextView tvTime = item.findViewById(R.id.tvCommentTime);
            TextView tvText = item.findViewById(R.id.tvCommentText);
            ImageButton btnOpt = item.findViewById(R.id.btnCommentOptions);

            tvTime.setText(fmt.format(c.getCreateAt()));

            // Get user info from project members
            List<User> members = viewModel.getProjectMembers().getValue();
            if (members != null) {
                User commentUser = members.stream()
                        .filter(m -> m.getId() == c.getUserID())
                        .findFirst()
                        .orElse(null);

                if (commentUser != null) {
                    tvName.setText(commentUser.getFullname());
                    String avatarUrl = commentUser.getAvatar();
                    if (avatarUrl != null && !avatarUrl.isEmpty() && !avatarUrl.equals("img/avatar/default.png")) {
                        try {
                            avatar.setImage(android.net.Uri.parse(avatarUrl));
                        } catch (Exception e) {
                            avatar.setName(commentUser.getFullname());
                        }
                    } else {
                        avatar.setName(commentUser.getFullname());
                    }
                }
            }

            // Handle file/image in comment
            String content = c.getContent();
            tvText.setText(styleTagsOnShowComment(content, this, tvText));
            tvText.setMovementMethod(LinkMovementMethod.getInstance());
            tvText.setHighlightColor(Color.TRANSPARENT); // Xóa highlight khi click

            btnOpt.setOnClickListener(v -> showCommentOptions(v, c));
            binding.commentContainer.addView(item);
        }
    }

    private String truncateFileName(String fileName, int maxLength) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        if (fileName.length() <= maxLength) {
            return fileName;
        }

        // Tìm vị trí dấu chấm cuối cùng (phần mở rộng)
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            // Nếu không có phần mở rộng, cắt bớt ở cuối
            return fileName.substring(0, maxLength - 3) + "...";
        }

        // Lấy phần mở rộng
        String extension = fileName.substring(lastDotIndex);
        // Lấy phần tên file (không có phần mở rộng)
        String nameWithoutExt = fileName.substring(0, lastDotIndex);

        // Tính toán độ dài tối đa cho phần tên file
        int maxNameLength = maxLength - extension.length() - 3; // 3 là độ dài của "..."

        if (nameWithoutExt.length() <= maxNameLength) {
            return fileName;
        }

        // Cắt bớt phần tên file và thêm "..."
        return nameWithoutExt.substring(0, maxNameLength) + "..." + extension;
    }

    private void showFileDetailDialog(int fileId) {
        List<File> files = viewModel.getFiles().getValue();
        if (files == null) return;

        // Tìm file dựa trên file id (không phân biệt hoa thường)
        File targetFile = files.stream().filter(f -> f.getId() == fileId).findFirst().orElse(null);

        if (targetFile == null) {
            showToast(">>> File not found: " + targetFile.getFileName());
            return;
        }

        // Create and show dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_file_detail);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Set file information
        ImageView ivFileIcon = dialog.findViewById(R.id.ivFileIcon);
        TextView tvFileName = dialog.findViewById(R.id.tvFileName);
        TextView tvFileSize = dialog.findViewById(R.id.tvFileSize);
        TextView tvFileType = dialog.findViewById(R.id.tvFileType);
        TextView tvFileDate = dialog.findViewById(R.id.tvFileDate);
        Button btnDownload = dialog.findViewById(R.id.btnDownload);
        Button btnDelete = dialog.findViewById(R.id.btnDelete);

        // Set file icon based on type
        int iconRes = getFileIconResource(targetFile.getFileType());
        ivFileIcon.setImageResource(iconRes);

        // Thu gọn tên file nếu quá dài (giới hạn 30 ký tự)
        String truncatedName = truncateFileName(targetFile.getFileName(), 30);
        tvFileName.setText(truncatedName);
        tvFileSize.setText("Kích thước: " + formatFileSize(targetFile.getFileSize()));
        tvFileType.setText("Loại file: " + targetFile.getFileType().toUpperCase());
        tvFileDate.setText("Ngày tạo: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(targetFile.getCreatedAt()));

        // Handle download button
        btnDownload.setOnClickListener(v -> {
            showToast("Đang tải xuống file...");
        });

        // Handle delete button
        btnDelete.setOnClickListener(v -> {
            // Xóa file
            viewModel.deleteFile(targetFile);

            String fileName = targetFile.getFileName();

            // Cập nhật comment chứa file này
            List<Comment> comments = viewModel.getComments().getValue();
            if (comments != null) {
                for (Comment comment : comments) {
                    String content = comment.getContent();
                    if (content.contains("[File]" + fileName + "[/File]")) {
                        // Xóa phần [File] và tên file khỏi comment
                        String newContent = content.replace("[File]" + fileName + "[/File]", "").trim();
                        if (newContent.isEmpty()) {
                            // Nếu comment trống sau khi xóa file, xóa luôn comment
                            viewModel.deleteComment(comment);
                        } else {
                            // Cập nhật nội dung comment
                            viewModel.editComment(comment, newContent);
                        }
                        break;
                    }
                }
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    private int getFileIconResource(String fileType) {
        switch (fileType.toLowerCase()) {
            case "pdf":
                return R.drawable.ic_pdf;
            case "doc":
            case "docx":
                return R.drawable.ic_word;
            case "xls":
            case "xlsx":
                return R.drawable.ic_excel;
            case "ppt":
            case "pptx":
                return R.drawable.ic_powerpoint;
            case "txt":
                return R.drawable.ic_text;
            case "zip":
            case "rar":
                return R.drawable.ic_zip;
            default:
                return R.drawable.ic_file;
        }
    }

    private void hideCmtSection() {
        binding.commentContainer.removeAllViews();
        binding.commentContainer.setVisibility(View.GONE);
    }

    private void toggleCmtSection() {
        List<Comment> comments = viewModel.getComments().getValue();
        if (comments == null || comments.isEmpty()) {
            hideCmtSection();
        } else if (viewModel.getIsCommentsExpanded().getValue()) {
            hideCmtSection();
            viewModel.toggleCommentsExpanded();
        } else {
            showAllComments();
            viewModel.toggleCommentsExpanded();
        }
    }

    private void showCommentOptions(View anchor, Comment c) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.menu_comment_item, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit_comment) {
                editComment(c);
                return true;
            } else if (item.getItemId() == R.id.action_delete_comment) {
                deleteComment(c);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void editComment(Comment c) {
        // TODO: Implement edit comment dialog
    }

    private void deleteComment(Comment c) {
        viewModel.deleteComment(c);
    }

    private void initEdgeToEdge() {
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(),
                (view, insets) -> {
                    Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    view.setPadding(sys.left, sys.top, sys.right, sys.bottom);
                    return insets;
                }
        );
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbarTask);
        Drawable nav = binding.toolbarTask.getNavigationIcon();
        if (nav != null) {
            nav.setTint(ContextCompat.getColor(this, R.color.black));
            binding.toolbarTask.setNavigationIcon(nav);
        }
        binding.toolbarTask.setNavigationOnClickListener(v -> finish());
    }

    private void initAdapters() {
        binding.rvImageAttachments.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        binding.rvImageAttachments.setNestedScrollingEnabled(false);
        imageAdapter = new ImageAttachmentAdapter(this, new ArrayList<>(), new ImageAttachmentAdapter.OnAttachmentActionListener() {
            @Override
            public void onDownloadClicked(int position) {
                List<Uri> uris = viewModel.getImageUris().getValue();
                if (uris != null && position < uris.size()) {
                    Uri uri = uris.get(position);
                    // TODO: xử lý tải xuống ảnh
                }
            }

            @Override
            public void onDeleteClicked(int position) {
                viewModel.removeImageUri(position);
            }
        });
        binding.rvImageAttachments.setAdapter(imageAdapter);

        binding.rvFileAttachments.setLayoutManager(
                new LinearLayoutManager(this)
        );
        binding.rvFileAttachments.setNestedScrollingEnabled(false);
        fileAdapter = new FileAttachmentAdapter(this, new ArrayList<>(), new FileAttachmentAdapter.OnAttachmentActionListener() {
            @Override
            public void onDownloadClicked(int position) {
                List<File> files = viewModel.getFiles().getValue();
                if (files != null && position < files.size()) {
                    // TODO: download logic
                }
            }

            @Override
            public void onDeleteClicked(int position) {
                List<File> files = viewModel.getFiles().getValue();
                if (files != null && position < files.size()) {
                    viewModel.deleteFile(files.get(position));
                }
            }
        });
        binding.rvFileAttachments.setAdapter(fileAdapter);

        // Observe image URIs
        viewModel.getImageUris().observe(this, uris -> {
            imageAdapter.updateUris(uris);
        });

        // Observe files
        viewModel.getFiles().observe(this, files -> {
            fileAdapter.updateFiles(files);
        });
    }

    private void registerPickers() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        for (Uri uri : uris) {
                            String fileName = getFileName(uri);
                            String mimeType = getContentResolver().getType(uri);
                            if (mimeType != null && mimeType.startsWith("image/")) {
                                // Add image to task
                                try {
                                    FileService.uploadFile(this, savedTask.getTaskID(), getContentResolver().openInputStream(uri), fileName, mimeType, res -> {
                                        try {
                                            String jsonString = new String(res.data, StandardCharsets.UTF_8);
                                            JSONObject jsonObject = new JSONObject(jsonString);
                                            JSONObject data = jsonObject.optJSONObject("data"); // nếu là object
                                            String filepath = String.valueOf(data.optString("filePath"));
                                            Log.d(TAG, ">>> data: " + (data != null ? data.toString() : "null"));

                                            addImageAsUri(FileConvertor.fromJson(data));

                                            // Add image reference to comment
                                            appendFileToComment("[Image] " + fileName, data.optInt("id"));

                                            // Show image section
                                            viewModel.toggleImagesExpanded();
                                        } catch (Exception e) {
                                            Log.e(TAG, ">>> JSON parse error", e);
                                        }
                                        ;
                                    }, err -> {
                                        Log.d(TAG, ">>> fail to upload image: " + err);
                                    });
                                } catch (FileNotFoundException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                }
        );

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        for (Uri uri : uris) {
                            String fileName = getFileName(uri);
                            String mimeType = getContentResolver().getType(uri);

                            if (mimeType != null && mimeType.startsWith("image/")) {
                                showToast("File \"" + fileName + "\" là ảnh, vui lòng chọn mục tải ảnh");
                                continue;
                            }

                            // Add document (file) to task
                            try {
                                FileService.uploadFile(this, savedTask.getTaskID(), getContentResolver().openInputStream(uri), fileName, mimeType,
                                        res -> {
                                            String jsonString = new String(res.data, StandardCharsets.UTF_8);
                                            JSONObject jsonObject = null;
                                            try {
                                                jsonObject = new JSONObject(jsonString);
                                            } catch (JSONException e) {
                                                Log.e(TAG, ">>> JSON parse error at file upload response: " + e);
                                            }
                                            JSONObject data = jsonObject.optJSONObject("data"); // nếu là object
                                            Log.d(TAG, ">>> file uploaded just now: " + data);
                                            File file = FileConvertor.fromJson(data);

                                            // Add file to task
                                            addDocument(file);

                                            // Add file reference to comment
                                            appendFileToComment("[File] " + fileName, data.optInt("id"));

                                            // Show file section
                                            viewModel.toggleFilesExpanded();
                                        }, err -> {
                                        });
                            } catch (Exception e) {
                                Log.d(TAG, ">>> fail to add document to task: " + e.getMessage());
                            }
                        }
                    }
                }
        );
    }

    private void addDocument(File file) {
        viewModel.addDocument(file);
    }

    private void addImageAsUri(File file) {
        viewModel.addImageAsUri(file);
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf(java.io.File.separator);
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private String getFileExtension(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        if (mimeType != null) {
            if (mimeType.contains("pdf")) return "pdf";
            if (mimeType.contains("word")) return "doc";
            if (mimeType.contains("excel") || mimeType.contains("spreadsheet")) return "xls";
            if (mimeType.contains("powerpoint") || mimeType.contains("presentation")) return "ppt";
            if (mimeType.contains("text")) return "txt";
            if (mimeType.contains("zip") || mimeType.contains("compressed")) return "zip";
        }
        return "bin";
    }

    private long getFileSize(Uri uri) {
        try {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    return cursor.getLong(sizeIndex);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format(Locale.getDefault(), "%.1f %s",
                size / Math.pow(1024, digitGroups),
                units[digitGroups]);
    }

    private void setupListeners() {
        binding.rowMember.setOnClickListener(v -> showMemberSelectionDialog());
        binding.rowDueDate.setOnClickListener(v -> showDateTimePicker());
//        binding.llMainFiles.setOnClickListener(v -> uploadFileFromDevice());

        binding.rowImageAttachments.setOnClickListener(v -> viewModel.toggleImagesExpanded());
        binding.rowFileAttachments.setOnClickListener(v -> viewModel.toggleFilesExpanded());
        binding.rowComments.setOnClickListener(v -> viewModel.toggleCommentsExpanded());
        binding.btnMove.setOnClickListener(v -> showMoveTaskDialog());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.menu_task, menu);

        // Set color for delete item
        MenuItem delete = menu.findItem(R.id.action_delete);
        SpannableString deleteText = new SpannableString(delete.getTitle());
        deleteText.setSpan(new ForegroundColorSpan(
                ContextCompat.getColor(this, R.color.red)
        ), 0, deleteText.length(), 0);
        delete.setTitle(deleteText);

        // Set color for update item
        MenuItem edit = menu.findItem(R.id.action_update_title);
        SpannableString editText = new SpannableString(edit.getTitle());
        editText.setSpan(new ForegroundColorSpan(
                ContextCompat.getColor(this, R.color.colorAccent)
        ), 0, editText.length(), 0);
        edit.setTitle(editText);

        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null && "MenuBuilder".equals(menu.getClass().getSimpleName())) {
            try {
                Method m = menu.getClass()
                        .getDeclaredMethod("setOptionalIconsVisible", boolean.class);
                m.setAccessible(true);
                m.invoke(menu, true);
            } catch (Exception ignored) {
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_move) {
            showMoveTaskDialog();
            return true;
        } else if (id == R.id.action_update_title) {
            showEditTaskTitleDialog();
            return true;
        } else if (id == R.id.action_delete) {
            showDeleteTaskDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showImageSection() {
        binding.rowImageAttachments.setVisibility(View.VISIBLE);
        binding.separatorImage.setVisibility(View.VISIBLE);
        binding.rvImageAttachments.setVisibility(View.VISIBLE);
        binding.ivExpandImages.setImageResource(R.drawable.ic_expand_less);
        imageAdapter.notifyDataSetChanged();
    }

    private void hideImageSection() {
        binding.separatorImage.setVisibility(View.GONE);
        binding.rvImageAttachments.setVisibility(View.GONE);
        binding.ivExpandImages.setImageResource(R.drawable.ic_expand_more);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showFileSection() {
        binding.rowFileAttachments.setVisibility(View.VISIBLE);
        binding.separatorFile.setVisibility(View.VISIBLE);
        binding.rvFileAttachments.setVisibility(View.VISIBLE);
        binding.ivExpandFiles.setImageResource(R.drawable.ic_expand_less);
        fileAdapter.notifyDataSetChanged();
    }

    private void hideFileSection() {
        binding.separatorFile.setVisibility(View.GONE);
        binding.rvFileAttachments.setVisibility(View.GONE);
        binding.ivExpandFiles.setImageResource(R.drawable.ic_expand_more);
    }

    private void showImageUploadDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Upload image")
                .setItems(new String[]{getString(R.string.upload), getString(R.string.cancel)},
                        (dlg, w) -> {
                            if (w == 0) imagePickerLauncher.launch(new String[]{"image/*"});
                        }
                ).show();
    }

    private void showFileUploadDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Upload file")
                .setItems(new String[]{getString(R.string.upload), getString(R.string.cancel)},
                        (dlg, w) -> {
                            if (w == 0) filePickerLauncher.launch(new String[]{"*/*"});
                        }
                ).show();
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void uploadFileFromDevice() {
        new AlertDialog.Builder(this)
                .setTitle("Tải file lên")
                .setItems(new String[]{"Chọn ảnh", "Chọn file", "Hủy"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // Chọn ảnh
                            imagePickerLauncher.launch(new String[]{"image/*"});
                            break;
                        case 1: // Chọn file
                            filePickerLauncher.launch(new String[]{"*/*"});
                            break;
                    }
                })
                .show();
    }

    private void loadSampleData() {
        binding.imgAvatar.setName("Nguyễn Thành Trung");

        // Sample image URIs
        List<Uri> sampleUris = new ArrayList<>();
        sampleUris.add(Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.calendar));
        sampleUris.add(Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.clock));
        sampleUris.add(Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.calendar));
        sampleUris.add(Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.calendar));
        sampleUris.add(Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.clock));
        sampleUris.add(Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.calendar));
        viewModel.updateImageUris(sampleUris);

        // Sample files
        List<File> sampleFiles = new ArrayList<>();
        sampleFiles.add(new File(1, "Report.pdf", "content://com.example.yourapp/files/Report.pdf", 2300L, "pdf", 1, 1, new Date(), new Date()));
        sampleFiles.add(new File(2, "Note.txt", "content://com.example.yourapp/files/Note.txt", 121L, "txt", 1, 1, new Date(), new Date()));
        sampleFiles.add(new File(3, "Report.pdf", "content://com.example.yourapp/files/Report.pdf", 2300L, "pdf", 1, 1, new Date(), new Date()));
        sampleFiles.add(new File(4, "Note.txt", "content://com.example.yourapp/files/Note.txt", 121L, "txt", 1, 1, new Date(), new Date()));
        sampleFiles.add(new File(5, "Report.pdf", "content://com.example.yourapp/files/Report.pdf", 2300L, "pdf", 1, 1, new Date(), new Date()));
        sampleFiles.add(new File(6, "Note.txt", "content://com.example.yourapp/files/Note.txt", 121L, "txt", 1, 1, new Date(), new Date()));
        viewModel.updateFiles(sampleFiles);

        // Show sections
        viewModel.toggleImagesExpanded();
        viewModel.toggleFilesExpanded();
    }

    private void updateCardStrokeColor(boolean isChecked) {
        int colorResId = isChecked
                ? R.color.card_stroke_checked
                : R.color.card_stroke_default;
        // 1. Đổi màu viền
        binding.cardMain.setStrokeColor(
                ContextCompat.getColor(this, colorResId)
        );
        // 2. Đổi độ dày viền (ví dụ 4dp)
        int strokeWidthDp = 2;
        int strokeWidthPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                strokeWidthDp,
                getResources().getDisplayMetrics()
        );
        binding.cardMain.setStrokeWidth(strokeWidthPx);
    }

    private void showMoveTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_move_task, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        // Điều chỉnh kích thước dialog
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(layoutParams);
        }

        Toolbar toolbar = dialogView.findViewById(R.id.toolbar_move_task);
        ImageView btnConfirm = dialogView.findViewById(R.id.btn_confirm_move);
        Spinner spinnerPhase = dialogView.findViewById(R.id.spinner_phase);
        Spinner spinnerPosition = dialogView.findViewById(R.id.spinner_position);

        // Lấy task hiện tại và danh sách phases
        Task currentTask = viewModel.getTask().getValue();
        List<Phase> allPhases = viewModel.getAllProjectPhases().getValue();

        // --- Debugging Toasts --- START
        if (currentTask != null) {
            showToast("Current Task ID: " + currentTask.getTaskID() + ", Phase ID: " + currentTask.getPhaseID());
        } else {
            showToast("Current Task is null");
        }

        if (allPhases != null) {
            for (Phase phase : allPhases) {
                showToast("Phase ID: " + phase.getPhaseID() + ", Name: " + phase.getPhaseName() + ", Tasks Size: " + (phase.getTasks() != null ? phase.getTasks().size() : "null"));
            }
        } else {
            showToast("All Phases list is null");
        }
        // --- Debugging Toasts --- END

        if (currentTask == null || allPhases == null || allPhases.isEmpty()) {
            showToast("Không thể di chuyển thẻ: Thiếu dữ liệu.");
            dialog.dismiss();
            return;
        }

        // Tạo danh sách tên phase cho spinner
        List<String> phaseNames = new ArrayList<>();
        int currentPhaseIndex = -1;
        for (int i = 0; i < allPhases.size(); i++) {
            Phase phase = allPhases.get(i);
            String phaseName = phase.getPhaseName();
            Log.d("Check ..........>>>>>>", "vi tri " + i);
            if (currentTask.getPhaseID() == phase.getPhaseID()) {
                phaseName += " (hiện tại)";
                currentPhaseIndex = i;
                Log.d("Check ..........>>>>>>", "Hien tai " + currentPhaseIndex);
            }
            phaseNames.add(phaseName);
        }

        ArrayAdapter<String> phaseAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, phaseNames);
        phaseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPhase.setAdapter(phaseAdapter);

        // Đặt lựa chọn mặc định cho spinner phase
        if (currentPhaseIndex != -1) {
            spinnerPhase.setSelection(currentPhaseIndex);
        }

        // Listener cho spinner phase
        spinnerPhase.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Phase selectedPhase = allPhases.get(position);
                int maxPosition = selectedPhase.getTasks().size() + 1;

                List<String> positions = new ArrayList<>();
                for (int i = 1; i <= maxPosition; i++) {
                    positions.add(String.valueOf(i));
                }

                ArrayAdapter<String> positionAdapter = new ArrayAdapter<>(TaskActivity.this,
                        android.R.layout.simple_spinner_item, positions);
                positionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerPosition.setAdapter(positionAdapter);

                // Nếu là phase hiện tại, chọn vị trí của task hiện tại
                if (selectedPhase.getPhaseID() == currentTask.getPhaseID()) {
                    int currentTaskOrderIndex = currentTask.getOrderIndex();
                    if (currentTaskOrderIndex >= 1 && currentTaskOrderIndex <= maxPosition) {
                        spinnerPosition.setSelection(currentTaskOrderIndex - 1);
                    }
                } else {
                    spinnerPosition.setSelection(maxPosition - 1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Xử lý nút xác nhận
        btnConfirm.setOnClickListener(v -> {
            Phase selectedPhase = allPhases.get(spinnerPhase.getSelectedItemPosition());
            int newPosition = Integer.parseInt(spinnerPosition.getSelectedItem().toString());

            // TODO: Logic để di chuyển task trong TaskRepository/ViewModel
            // Cần xóa task khỏi phase cũ và thêm vào phase mới ở vị trí mới
            // Sau đó cập nhật cả Project (nếu ProjectActivity đang lắng nghe các thay đổi này)
            viewModel.updateTaskPhaseAndOrder(selectedPhase.getPhaseID(), newPosition);
            showToast("Đã di chuyển thẻ!");
            dialog.dismiss();
            finish(); // Hoặc cập nhật UI thay vì finish nếu muốn giữ nguyên TaskActivity
        });

        // Xử lý nút đóng
        toolbar.setNavigationOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showMemberSelectionDialog() {
        Task currentTask = viewModel.getTask().getValue();
        if (currentTask == null) return;

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.dialog_select_member, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        RecyclerView rvMembers = bottomSheetView.findViewById(R.id.rv_members);
        Button btnComplete = bottomSheetView.findViewById(R.id.btn_complete);

        // Tối ưu RecyclerView
        rvMembers.setHasFixedSize(true);
        rvMembers.setItemViewCacheSize(20);
        rvMembers.setDrawingCacheEnabled(true);
        rvMembers.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        // Observer để load lại khi ViewModel thay đổi
        viewModel.getProjectMembers().observe(this, members -> {
            if (members == null) return;

            adapter = new TaskMemberAdapter(members, member -> {
                if (member == null) {
                    // Khi bỏ chọn thành viên
                    btnComplete.setEnabled(true);
                    return;
                }

                int clickedId = member.getId();
                int prevAssigneeId = currentTask.getAssignedTo();

                // Nếu click vào người đang được gán
                if (clickedId == prevAssigneeId) {
                    // Bỏ chọn người đang được gán
                    adapter.deselectAll();
                } else {
                    // Chọn người khác
                    btnComplete.setEnabled(true);
                }
            });

            rvMembers.setLayoutManager(new LinearLayoutManager(this));
            rvMembers.setAdapter(adapter);

            // Đảm bảo adapter đã được set trước khi chọn thành viên
            rvMembers.post(() -> {
                // Nếu đã có người được gán, đánh dấu ngay lúc load
                if (currentTask.getAssignedTo() != 0) {
                    adapter.selectMemberById(currentTask.getAssignedTo());
                    btnComplete.setEnabled(false); // Ban đầu disable nút Complete
                } else {
                    btnComplete.setEnabled(false);
                }
            });

            // Xử lý khi bấm nút Complete
            btnComplete.setOnClickListener(v -> {
                User sel = adapter.getSelectedMember();
                int prevAssigneeId = currentTask.getAssignedTo();

                // Nếu không có người được chọn và trước đó có người được gán
                if (sel == null && prevAssigneeId != 0) {
                    // Bỏ gán task
                    btnComplete.setEnabled(false);
                    toggleRemove(prevAssigneeId, currentTask, adapter);
                }
                // Nếu có người được chọn và khác với người đang được gán
                else if (sel != null && sel.getId() != prevAssigneeId) {
                    // Gán task cho người mới
                    btnComplete.setEnabled(false);
                    addNewAssignee(currentTask, sel, bottomSheetDialog, btnComplete);
                }
            });
        });

        bottomSheetDialog.setOnDismissListener(d -> {
            // Cleanup khi dialog đóng
            if (adapter != null) {
                adapter.deselectAll();
            }
            // Clear cache và giải phóng bộ nhớ
            rvMembers.setAdapter(null);
            rvMembers.clearOnScrollListeners();
            rvMembers.removeAllViews();
        });

        bottomSheetDialog.show();
    }

    /**
     * Khi click vào item đang gán: gọi remove luôn
     */
    private void toggleRemove(int userId,
                              Task currentTask,
                              TaskMemberAdapter adapter) {
        // Disable UI
        adapter.setItemsEnabled(false);
        // Gọi API remove
        ProjectMemberService.removeMemberInTask(
                this,
                currentTask.getTaskID(),
                userId,
                resp -> {
                    // Remove thành công
                    currentTask.setAssignedTo(0);
                    viewModel.updateTask(currentTask);           // đẩy lên ViewModel
                    updateMemberUI(null);                        // xóa hiển thị assignee
                    adapter.deselectAll();                       // bỏ highlight
                    adapter.setItemsEnabled(true);               // enable lại UI
                },
                err -> {
                    Toast.makeText(this, "Không thể bỏ gán thành viên", Toast.LENGTH_SHORT).show();
                    adapter.setItemsEnabled(true);
                    Log.e("TaskActivity", "removeMemberErr", err);
                }
        );
    }

    /**
     * Add assignee mới, sau đó update UI/Model và đóng dialog
     */
    private void addNewAssignee(Task currentTask,
                                User selectedMember,
                                BottomSheetDialog dialog,
                                Button btnComplete) {
        ProjectMemberService.addMember(
                this,
                currentTask.getTaskID(),
                selectedMember.getId(),
                ProjectHolder.get().getProjectID(),
                response -> {
                    // Thành công
                    currentTask.setAssignedTo(selectedMember.getId());
                    viewModel.updateTask(currentTask);    // đẩy lên ViewModel
                    updateMemberUI(selectedMember);
                    dialog.dismiss();
                },
                err -> {
                    String msg = "Lỗi thêm thành viên";
                    try {
                        msg = Helpers.parseError(err);
                    } catch (Exception ignored) {
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    btnComplete.setEnabled(true);
                    Log.e("TaskActivity", "addMemberErr", err);
                }
        );
    }

    private void updateMemberUI(User member) {
        if (member != null) {
            binding.tvThanhvien.setVisibility(View.GONE);
            binding.avThanhvien.setVisibility(View.VISIBLE);
            if (member.getAvatar() != null && !member.getAvatar().isEmpty() && !member.getAvatar().equals("img/avatar/default.png")) {
                // TODO: Load avatar using your image loading library
                // Glide.with(binding.avThanhVien).load(member.getAvatar()).into(binding.avThanhVien);
            } else {
                binding.avThanhvien.setName(member.getFullname() != null ? member.getFullname() : "lam dat");
            }
        } else {
            binding.tvThanhvien.setVisibility(View.VISIBLE);
            binding.avThanhvien.setVisibility(View.GONE);
        }
    }

    private void initMemberUI() {
        Task currentTask = viewModel.getTask().getValue();
        if (currentTask == null) return;

        if (currentTask.getAssignedTo() != 0) {
            // Find member in the list
            List<User> members = viewModel.getProjectMembers().getValue();
            if (members != null) {
                User assignedMember = members.stream()
                        .filter(m -> m.getId() == currentTask.getAssignedTo())
                        .findFirst()
                        .orElse(null);

                if (assignedMember != null) {
                    updateMemberUI(assignedMember);
                }
            }
        } else {
            binding.tvThanhvien.setVisibility(View.VISIBLE);
            binding.avThanhvien.setVisibility(View.GONE);
        }
    }

    private void showDateTimePicker() {
        // Show date picker first
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(year, month, dayOfMonth);

                    // Check if selected date is in the past
                    Calendar now = Calendar.getInstance();
                    if (calendar.before(now)) {
                        showToast("Ngày hết hạn phải lớn hơn thời gian hiện tại");
                        return;
                    }

                    // After selecting date, show time picker
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            this,
                            (view1, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);

                                // Check if selected date and time is in the past
                                if (calendar.before(now)) {
                                    showToast("Thời gian hết hạn phải lớn hơn thời gian hiện tại");
                                    return;
                                }

                                // Format date and time
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

                                String dateStr = dateFormat.format(calendar.getTime());
                                String timeStr = timeFormat.format(calendar.getTime());

                                // Update UI and ViewModel
                                binding.tvDueDate.setText(String.format("Đến hạn %s lúc %s", dateStr, timeStr));
                                viewModel.updateTaskDueDate(calendar.getTime());
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                    );
                    timePickerDialog.show();
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void setupCommentBar() {
        // Set current user's avatar
        User currentUser = userPreferences.getUser();
        if (currentUser != null) {
            String avatarUrl = currentUser.getAvatar();
            if (avatarUrl != null && !avatarUrl.isEmpty() && !avatarUrl.equals("img/avatar/default.png")) {
                try {
                    binding.imgAvatar.setImage(android.net.Uri.parse(avatarUrl));
                } catch (Exception e) {
                    binding.imgAvatar.setName(currentUser.getFullname());
                }
            } else {
                binding.imgAvatar.setName(currentUser.getFullname());
            }
        }

        // Handle keyboard visibility
        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            binding.getRoot().getWindowVisibleDisplayFrame(r);
            int screenHeight = binding.getRoot().getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > screenHeight * 0.15) {
                // Keyboard is visible
                binding.commentBar.setTranslationY(-keypadHeight + binding.commentBar.getHeight());
            } else {
                // Keyboard is hidden
                binding.commentBar.setTranslationY(0);
            }
        });

        // Handle comment input focus
        binding.etComment.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                fullComment = binding.etComment.getText().toString();
                imm.showSoftInput(getEtComment(), InputMethodManager.SHOW_IMPLICIT);
            }
        });

        binding.etComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Trước khi thay đổi (thường không cần xử lý)
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Giống như onChange trong React: lắng nghe nội dung đang thay đổi
//                Log.d(TAG, ">>> new comment: " + currentText);
                fullComment = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Sau khi thay đổi xong (nếu muốn xử lý cuối cùng)
            }
        });


        // Handle send button
        binding.btnSend.setOnClickListener(v -> {
            Log.d(TAG, ">>> full comment: " + fullComment);
            String commentText = fullComment.trim();
            if (!commentText.isEmpty()) {
                CommentService.createComment(this, savedTask.getTaskID(), commentText, false, res -> {
                    try {
                        Comment comment = CommentConvertor.fromJson(res.optJSONObject("data"));
                        viewModel.addComment(comment, userPreferences.getUser().getId());
                        binding.etComment.setText("");
                        imm.hideSoftInputFromWindow(binding.etComment.getWindowToken(), 0);
                    } catch (Exception e) {
                    }
                }, err -> {
                    Log.d(TAG, ">>> create Comment Err: " + err.getMessage());
                    String errMsg = "Không thể tạo bình luận";
                    try {
                        errMsg = Helpers.parseError(err);
                    } catch (Exception e) {
                    }
                    Toast.makeText(this, errMsg, Toast.LENGTH_SHORT).show();
                });
            }
        });

        // Handle attach button
        binding.btnAttach.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Tải file lên")
                    .setItems(new String[]{"Chọn ảnh", "Chọn file", "Hủy"}, (dialog, which) -> {
                        switch (which) {
                            case 0: // Chọn ảnh
                                imagePickerLauncher.launch(new String[]{"image/*"});
                                break;
                            case 1: // Chọn file
                                filePickerLauncher.launch(new String[]{"*/*"});
                                break;
                        }
                    })
                    .show();
        });
    }

    @NonNull
    private EditText getEtComment() {
        return binding.etComment;
    }

    private void appendFileToComment(String text, int fileId) {
        String currentComment = binding.etComment.getText().toString();

        String fileName = text.substring(text.indexOf("]") + 2);
        String truncatedFileName = truncateFileName(fileName, 20);
        text = "[File:id=" + fileId + "]" + truncatedFileName + "[/File]";
        String updatedComment = currentComment.isEmpty() ? text : currentComment + text;

        fullComment = updatedComment;
        binding.etComment.setText(styleTagsOnWriteComment(updatedComment, this));

        // Move cursor to end
        binding.etComment.setSelection(binding.etComment.getText().length());
    }

    private SpannableString styleTagsOnWriteComment(String rawText, Context context) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        // Dùng regex để phát hiện các tag [File]...[/File] và [Image]...[/Image]
        Pattern pattern = Pattern.compile("\\[(File|Image)](.*?)\\[/\\1]");
        Matcher matcher = pattern.matcher(rawText);

        int lastEnd = 0;

        while (matcher.find()) {
            // Thêm phần text không chứa tag
            builder.append(rawText, lastEnd, matcher.start());

            String tagType = matcher.group(1);     // "File" hoặc "Image"
            String innerText = matcher.group(2);   // Nội dung bên trong tag

            int spanStart = builder.length();
            builder.append(innerText);
            int spanEnd = builder.length();

            // Style: Gạch chân + Tô màu
            builder.setSpan(new UnderlineSpan(), spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(
                    new ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorAccent)),
                    spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            lastEnd = matcher.end();
        }

        // Thêm phần còn lại sau thẻ cuối cùng
        if (lastEnd < rawText.length()) {
            builder.append(rawText.substring(lastEnd));
        }

        return SpannableString.valueOf(builder);
    }

    private SpannableString styleTagsOnShowComment(String rawText, Context context, TextView textView) {
        SpannableStringBuilder resultBuilder = new SpannableStringBuilder();

        // Regex hỗ trợ cả [File] / [File:id=123] / [Image] / [Image:id=456]
        Pattern pattern = Pattern.compile("\\[(File|Image)(?::id=(\\d+))?](.+?)\\[/\\1]");
        Matcher matcher = pattern.matcher(rawText);

        int lastEnd = 0;

        while (matcher.find()) {
            resultBuilder.append(rawText, lastEnd, matcher.start());

            String tagType = matcher.group(1);     // File hoặc Image
            String tagId = matcher.group(2);       // có thể null
            String innerText = matcher.group(3);   // nội dung hiển thị

            int spanStart = resultBuilder.length();
            resultBuilder.append(innerText);
            int spanEnd = resultBuilder.length();

            // ClickableSpan cho cả File và Image
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    if (tagId != null) {
                        Log.d("ClickableSpan", ">>> hello click with id=" + tagId);

                        if ("File".equals(tagType)) {
                            showFileDetailDialog(Integer.parseInt(tagId));
                        } else if ("Image".equals(tagType)) {
                            showImageDetailDialog(Integer.parseInt(tagId));
                        }
                    } else {
                        Log.d("ClickableSpan", ">>> hello click");
                    }
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(true);
                    ds.setColor(ContextCompat.getColor(context, R.color.colorAccent));
                }
            };

            resultBuilder.setSpan(clickableSpan, spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            lastEnd = matcher.end();
        }

        // Thêm phần còn lại sau đoạn tag cuối cùng
        if (lastEnd < rawText.length()) {
            resultBuilder.append(rawText.substring(lastEnd));
        }

        textView.setMovementMethod(LinkMovementMethod.getInstance());

        return SpannableString.valueOf(resultBuilder);
    }

    private void showImageDetailDialog(int fileId) {
        List<Uri> imageUris = viewModel.getImageUris().getValue();
        if (imageUris == null) return;

        // Tìm ảnh dựa trên tên file (không phân biệt hoa thường)
        String imageName = "daddy";
        final Uri targetImage = imageUris.stream()
                .filter(uri -> {
                    // Lấy tên file gốc
                    String originalName = getFileName(uri);
                    // Thu gọn tên file để so sánh
                    String truncatedName = truncateFileName(originalName, 20);
                    // So sánh với tên file đã thu gọn
                    return truncatedName.equalsIgnoreCase(imageName);
                })
                .findFirst()
                .orElse(null);

        if (targetImage == null) {
            showToast("Không tìm thấy ảnh: " + imageName);
            return;
        }

        // Create and show dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_image_detail);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Set file information
        ImageView imgPreview = dialog.findViewById(R.id.imgPreview);
        TextView tvFileName = dialog.findViewById(R.id.tvFileName);
        TextView tvFileSize = dialog.findViewById(R.id.tvFileSize);
        TextView tvFileType = dialog.findViewById(R.id.tvFileType);
        TextView tvCreatedAt = dialog.findViewById(R.id.tvCreatedAt);
        Button btnDownload = dialog.findViewById(R.id.btnDownload);
        Button btnDelete = dialog.findViewById(R.id.btnDelete);
        ImageButton btnClose = dialog.findViewById(R.id.btnClose);

        // Set image preview
        imgPreview.setImageURI(targetImage);

        // Set file details
        String fileName = getFileName(targetImage);
        tvFileName.setText(fileName);
        tvFileSize.setText("Kích thước: " + formatFileSize(getFileSize(targetImage)));
        tvFileType.setText("Loại file: IMAGE");
        tvCreatedAt.setText("Ngày tạo: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date()));

        // Handle close button
        btnClose.setOnClickListener(v -> dialog.dismiss());

        // Handle download button
        btnDownload.setOnClickListener(v -> {
            showToast("Đang tải xuống ảnh...");
            dialog.dismiss();
        });

        // Handle delete button
        btnDelete.setOnClickListener(v -> {
            // Xóa ảnh
            int index = imageUris.indexOf(targetImage);
            if (index != -1) {
                viewModel.removeImageUri(index);
            }

            // Cập nhật comment chứa ảnh này
            List<Comment> comments = viewModel.getComments().getValue();
            if (comments != null) {
                for (Comment comment : comments) {
                    String content = comment.getContent();
                    if (content.contains("[Image]" + imageName + "[/Image]")) {
                        // Xóa phần [Image] và tên ảnh khỏi comment
                        String newContent = content.replace("[Image]" + imageName + "[/Image]", "").trim();
                        if (newContent.isEmpty()) {
                            // Nếu comment trống sau khi xóa ảnh, xóa luôn comment
                            viewModel.deleteComment(comment);
                        } else {
                            // Cập nhật nội dung comment
                            viewModel.editComment(comment, newContent);
                        }
                        break;
                    }
                }
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    private void showEditTaskTitleDialog() {
        Task currentTask = viewModel.getTask().getValue();
        if (currentTask == null) return;

        EditText input = new EditText(this);
        input.setText(currentTask.getTaskName());

        new AlertDialog.Builder(this)
                .setTitle("Chỉnh sửa tiêu đề")
                .setView(input)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newTitle = input.getText().toString().trim();
                    if (!newTitle.isEmpty()) {
                        // Gọi API cập nhật tiêu đề
                        TaskService.updateTask(
                                this,
                                currentTask.getTaskID(),
                                ProjectHolder.get().getProjectID(),
                                null, // Giữ nguyên mô tả
                                newTitle,
                                response -> {
                                    try {
                                        if ("success".equals(response.optString("status"))) {
                                            // Cập nhật UI ngay lập tức
                                            currentTask.setTaskName(newTitle);
                                            binding.toolbarTask.setTitle(newTitle);
                                            viewModel.updateTask(currentTask);
                                            showToast("Đã cập nhật tiêu đề");
                                            // Đánh dấu task đã được sửa đổi
                                            isTaskModified = true;
                                            Log.d(TAG, "Task title updated, isTaskModified set to true");
                                            // Set result ngay sau khi cập nhật thành công
                                            setResult(RESULT_OK);
                                            Log.d(TAG, "Setting result to RESULT_OK after title update");
                                        } else {
                                            String error = response.optString("error");
                                            showToast("Lỗi: " + error);
                                        }
                                    } catch (Exception e) {
                                        showToast("Lỗi xử lý dữ liệu");
                                        Log.e(TAG, "Error updating task title", e);
                                    }
                                },
                                error -> {
                                    String errMsg = "Không thể cập nhật tiêu đề";
                                    try {
                                        errMsg = Helpers.parseError(error);
                                    } catch (Exception ignored) {
                                    }
                                    showToast(errMsg);
                                    Log.e(TAG, "Error updating task title", error);
                                }
                        );
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteTaskDialog() {
        Task currentTask = viewModel.getTask().getValue();
        if (currentTask == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Xóa task")
                .setMessage("Bạn có chắc chắn muốn xóa task này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Gọi API xóa task
                    TaskService.deleteTask(
                            this,
                            currentTask.getTaskID(),
                            ProjectHolder.get().getProjectID(),
                            response -> {
                                try {
                                    if ("success".equals(response.optString("status"))) {
                                        showToast("Đã xóa task thành công");
                                        finish(); // Đóng màn hình task
                                    } else {
                                        String error = response.optString("error");
                                        showToast("Lỗi: " + error);
                                    }
                                } catch (Exception e) {
                                    showToast("Lỗi xử lý dữ liệu");
                                    Log.e(TAG, "Error deleting task", e);
                                }
                            },
                            error -> {
                                String errMsg = "Không thể xóa task";
                                try {
                                    errMsg = Helpers.parseError(error);
                                } catch (Exception ignored) {
                                }
                                showToast(errMsg);
                                Log.e(TAG, "Error deleting task", error);
                            }
                    );
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

}
