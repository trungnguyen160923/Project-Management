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
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.TypedValue;
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

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.example.projectmanagement.data.model.Comment;
import com.example.projectmanagement.data.model.File;
import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.data.model.Task;
import com.example.projectmanagement.data.model.User;
import com.example.projectmanagement.data.service.FileService;
import com.example.projectmanagement.databinding.ActivityTaskBinding;
import com.example.projectmanagement.ui.adapter.FileAttachmentAdapter;
import com.example.projectmanagement.ui.adapter.ImageAttachmentAdapter;
import com.example.projectmanagement.ui.adapter.TaskMemberAdapter;
import com.example.projectmanagement.ui.task.vm.TaskViewModel;
import com.example.projectmanagement.utils.Helpers;
import com.example.projectmanagement.viewmodel.AvatarView;
import com.example.projectmanagement.data.model.ProjectMemberHolder;

import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;

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

    private Task savedTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // Get task from intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Task task = extras.getParcelable("task");
            savedTask = task;
            if (task != null) {
                // Fetch task details from API
                viewModel.fetchTaskDetail(task.getTaskID());

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

        // Load comments from API
//        viewModel.fetchComments();

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(binding.etDescription, InputMethodManager.SHOW_IMPLICIT);

        // Set initial state of checkbox and card color based on task status
        viewModel.getTask().observe(this, task -> {
            if (task != null) {
                boolean isDone = "DONE".equalsIgnoreCase(task.getStatus());
                binding.checkboxCompleted.setChecked(isDone);
                updateCardStrokeColor(isDone);

                // Initialize member UI when task is loaded
                initMemberUI();
            }
        });

        // Listener for checkbox
        binding.checkboxCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateCardStrokeColor(isChecked);
            viewModel.updateTaskStatus(isChecked ? "DONE" : "WORKING");
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
            String text = binding.etDescription.getText().toString().trim();
            viewModel.updateTaskDescription(text);
            hideAndReset.onClick(v);
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
            if (description != null) {
                binding.etDescription.setText(description);
            }
        });

        viewModel.getTask().observe(this, task -> {
            if (task != null) {
                // Update UI when task changes
                initMemberUI();
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
                    avatar.setName(commentUser.getFullname());

                    if (commentUser.getAvatar() != null && !commentUser.getAvatar().isEmpty()) {
                        // TODO: Load avatar using your image loading library
                        // Glide.with(avatar).load(commentUser.getAvatar()).into(avatar);
                    }
                }
            }

            // Handle file/image in comment
            String content = c.getContent();
            SpannableString spannableString = new SpannableString(content);

            // Xử lý file
            if (content.contains("[File]")) {
                int fileStart = content.indexOf("[File]");
                int nameStart = fileStart + 6; // Sau "[File]"
                int nameEnd = content.indexOf("[/File]", nameStart);
                if (nameEnd == -1) nameEnd = content.length();

                // Lấy tên file gốc
                String fileName = content.substring(nameStart, nameEnd);

                // Thu gọn tên file
                String truncatedName = truncateFileName(fileName, 20);

                // Cập nhật nội dung với tên đã thu gọn
                content = content.substring(0, nameStart) + truncatedName + content.substring(nameEnd);
                spannableString = new SpannableString(content);

                // Thêm style cho tên file
                spannableString.setSpan(new UnderlineSpan(), nameStart, nameStart + truncatedName.length(), 0);
                spannableString.setSpan(
                        new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorAccent)),
                        nameStart, nameStart + truncatedName.length(), 0
                );

                // Thêm click listener cho file
                final String finalFileName = fileName;
                ClickableSpan fileClickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        showFileDetailDialog(finalFileName);
                    }
                };
                spannableString.setSpan(fileClickableSpan, nameStart, nameStart + truncatedName.length(), 0);
            }

            // Xử lý ảnh
            if (content.contains("[Image]")) {
                int imageStart = content.indexOf("[Image]");
                int nameStart = imageStart + 7; // Sau "[Image]"
                int nameEnd = content.indexOf("[/Image]", nameStart);
                if (nameEnd == -1) nameEnd = content.length();

                // Lấy thông tin ảnh (tên file và URI)
                String imageInfo = content.substring(nameStart, nameEnd);

                // Thu gọn tên ảnh để hiển thị
                String fileName = imageInfo.split("\\|")[0];
                String truncatedName = truncateFileName(fileName, 20);

                // Cập nhật nội dung với tên đã thu gọn
                content = content.substring(0, nameStart) + truncatedName + content.substring(nameEnd);
                spannableString = new SpannableString(content);

                // Thêm style cho tên ảnh
                spannableString.setSpan(new UnderlineSpan(), nameStart, nameStart + truncatedName.length(), 0);
                spannableString.setSpan(
                        new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorAccent)),
                        nameStart, nameStart + truncatedName.length(), 0
                );

                // Thêm click listener cho ảnh
                final String finalImageInfo = imageInfo;
                ClickableSpan imageClickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        showImageDetailDialog(finalImageInfo);
                    }
                };
                spannableString.setSpan(imageClickableSpan, nameStart, nameStart + truncatedName.length(), 0);
            }

            tvText.setText(spannableString);
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

    private void showFileDetailDialog(String fileName) {
        List<File> files = viewModel.getFiles().getValue();
        if (files == null) return;

        // Tìm file dựa trên tên file (không phân biệt hoa thường)
        final File targetFile = files.stream()
                .filter(file -> {
                    // Lấy tên file gốc
                    String originalName = file.getFileName();
                    // Thu gọn tên file để so sánh
                    String truncatedName = truncateFileName(originalName, 20);
                    // So sánh với tên file đã thu gọn
                    return truncatedName.equalsIgnoreCase(fileName);
                })
                .findFirst()
                .orElse(null);

        if (targetFile == null) {
            showToast("Không tìm thấy file: " + fileName);
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
            // TODO: Implement file download
            showToast("Đang tải xuống file...");
        });

        // Handle delete button
        btnDelete.setOnClickListener(v -> {
            // Xóa file
            viewModel.deleteFile(targetFile);

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

                                            String status = jsonObject.optString("status");
                                            JSONObject data = jsonObject.optJSONObject("data"); // nếu là object
                                            String error = jsonObject.optString("error");
                                            String filepath = String.valueOf(data.optString("filePath"));
                                            Log.d(TAG,">>> img url: "+Helpers.createImageUrlEndpoint(filepath));
//                                            Uri imgUri = null;
//                                            try {
//                                                 imgUri = FileService.downloadImageToMediaStore(this, Helpers.createImageUrlEndpoint(filepath), fileName, mimeType);
//                                            Log.d(TAG,">>> uri: "+imgUri);
//                                            }catch (Exception e){
//                                                Log.d(TAG,">>> "+ e.getMessage());
//                                            }
                                            Executor executor = Executors.newSingleThreadExecutor();
                                            executor.execute(() -> {


                                            Uri imgURI =  FileService.downloadImageAndGetUri(this,Helpers.createImageUrlEndpoint(filepath));

                                            viewModel.addImageUri(imgURI);

                                            Log.d(TAG, ">>> status: " + status);
                                            Log.d(TAG, ">>> data: " + (data != null ? data.toString() : "null"));
                                            Log.d(TAG, ">>> error: " + error);
                                            });
                                        } catch (Exception e) {
                                            Log.e(TAG, ">>> JSON parse error", e);
                                        }
                                        ;
                                    }, err -> {
                                        Log.d(TAG, ">>> err: " + err.toString());
                                    });
                                } catch (FileNotFoundException e) {
                                    throw new RuntimeException(e);
                                }

                                // Add image reference to comment
                                String imageComment = String.format("[Image] %s", fileName);
                                appendToComment(imageComment, true);

                                // Show image section
                                viewModel.toggleImagesExpanded();
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
                                showToast("File là ảnh, vui lòng chọn trong phần tải ảnh: " + fileName);
                                continue;
                            }

                            // Add file to task
                            String ext = getFileExtension(uri);
                            long size = getFileSize(uri);
                            File file = new File(
                                    viewModel.getFiles().getValue() != null ? viewModel.getFiles().getValue().size() + 1 : 1,
                                    fileName,
                                    uri.toString(),
                                    size,
                                    ext,
                                    1,
                                    1,
                                    new Date(),
                                    new Date()
                            );
                            viewModel.addFile(file);

                            // Add file reference to comment
                            String fileComment = String.format("[File] %s", fileName);
                            appendToComment(fileComment, true);

                            // Show file section
                            viewModel.toggleFilesExpanded();
                        }
                    }
                }
        );
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
        binding.llMainFiles.setOnClickListener(v -> uploadFileFromDevice());

        binding.rowImageAttachments.setOnClickListener(v -> viewModel.toggleImagesExpanded());
        binding.rowFileAttachments.setOnClickListener(v -> viewModel.toggleFilesExpanded());
        binding.rowComments.setOnClickListener(v -> viewModel.toggleCommentsExpanded());
        binding.btnMove.setOnClickListener(v -> showMoveTaskDialog());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.menu_task, menu);
        MenuItem delete = menu.findItem(R.id.action_delete);
        SpannableString s = new SpannableString(delete.getTitle());
        s.setSpan(new ForegroundColorSpan(
                ContextCompat.getColor(this, R.color.red)
        ), 0, s.length(), 0);
        delete.setTitle(s);
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

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_select_member);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        RecyclerView rvMembers = dialog.findViewById(R.id.rv_members);
        Button btnComplete = dialog.findViewById(R.id.btn_complete);

        // Observe project members from ViewModel
        viewModel.getProjectMembers().observe(this, members -> {
            if (members != null) {
                TaskMemberAdapter adapter = new TaskMemberAdapter(members, member -> {
                    // Handle member selection
                });

                rvMembers.setLayoutManager(new LinearLayoutManager(this));
                rvMembers.setAdapter(adapter);

                // Set initial selection if task has assigned member
                if (currentTask.getAssignedTo() != 0) {
                    int position = adapter.getPositionById(currentTask.getAssignedTo());
                    if (position != -1) {
                        adapter.selectMember(position);
                    }
                }

                btnComplete.setOnClickListener(v -> {
                    User selectedMember = adapter.getSelectedMember();
                    if (selectedMember != null) {
                        viewModel.assignTaskToMember(currentTask.getTaskID(), selectedMember.getId());
                        updateMemberUI(selectedMember);
                    }
                    dialog.dismiss();
                });
            }
        });

        dialog.setOnDismissListener(dialogInterface -> {
            // Handle dialog dismiss
        });

        dialog.show();
    }

    private void updateMemberUI(User member) {
        if (member != null) {
            binding.tvThanhvien.setVisibility(View.GONE);
            binding.avThanhvien.setVisibility(View.VISIBLE);

            if (member.getAvatar() != null && !member.getAvatar().isEmpty()) {
                // TODO: Load avatar using your image loading library
                // Glide.with(binding.avThanhVien).load(member.getAvatar()).into(binding.avThanhVien);
            } else {
                binding.avThanhvien.setName(member.getFullname());
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
                imm.showSoftInput(binding.etComment, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        // Handle send button
        binding.btnSend.setOnClickListener(v -> {
            String commentText = binding.etComment.getText().toString().trim();
            if (!commentText.isEmpty()) {
                viewModel.addComment(commentText);
                binding.etComment.setText("");
                imm.hideSoftInputFromWindow(binding.etComment.getWindowToken(), 0);
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

    private void appendToComment(String text, boolean isFile) {
        String currentText = binding.etComment.getText().toString();

        if (isFile) {
            // Nếu là file, kiểm tra xem đã có file trong comment chưa
            if (currentText.contains("[File]")) {
                // Nếu đã có file, thay thế file cũ
                int fileStart = currentText.indexOf("[File]");
                int fileEnd = currentText.indexOf("[/File]", fileStart);
                if (fileEnd == -1) fileEnd = currentText.length();

                // Thu gọn tên file mới
                String fileName = text.substring(text.indexOf("]") + 2);
                String truncatedName = truncateFileName(fileName, 20);
                String newFileText = "[File]" + truncatedName + "[/File]";

                // Thay thế file cũ bằng file mới
                currentText = currentText.substring(0, fileStart) + newFileText + currentText.substring(fileEnd + 7);
            } else {
                // Nếu chưa có file, thêm file mới
                String fileName = text.substring(text.indexOf("]") + 2);
                String truncatedName = truncateFileName(fileName, 20);
                text = "[File]" + truncatedName + "[/File]";
                currentText = currentText.isEmpty() ? text : currentText + text;
            }

            // Tạo SpannableString với style cho tên file
            SpannableString spannableString = new SpannableString(currentText);
            int fileStart = currentText.indexOf("[File]");
            int nameStart = fileStart + 6; // Sau "[File]"
            int nameEnd = currentText.indexOf("[/File]", nameStart);
            if (nameEnd == -1) nameEnd = currentText.length();

            // Thêm style cho tên file
            spannableString.setSpan(new UnderlineSpan(), nameStart, nameEnd, 0);
            spannableString.setSpan(
                    new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorAccent)),
                    nameStart, nameEnd, 0
            );

            binding.etComment.setText(spannableString);
        } else {
            // Nếu là text thông thường
            if (currentText.contains("[File]")) {
                // Nếu đã có file, thêm text vào sau file
                int fileEnd = currentText.indexOf("[/File]", currentText.indexOf("[File]")) + 7;
                if (fileEnd == -1) fileEnd = currentText.length();

                // Thêm text vào sau file
                currentText = currentText.substring(0, fileEnd) + text;
            } else {
                // Nếu chưa có file, thêm text bình thường
                currentText = currentText.isEmpty() ? text : currentText + text;
            }
            binding.etComment.setText(currentText);
        }

        // Move cursor to end
        binding.etComment.setSelection(binding.etComment.getText().length());
    }

    private void showImageDetailDialog(String imageName) {
        List<Uri> imageUris = viewModel.getImageUris().getValue();
        if (imageUris == null) return;

        // Tìm ảnh dựa trên tên file (không phân biệt hoa thường)
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
            // TODO: Implement image download
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
}
