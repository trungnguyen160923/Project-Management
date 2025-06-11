package com.example.projectmanagement.ui.task;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.Comment;
import com.example.projectmanagement.data.model.File;
import com.example.projectmanagement.databinding.ActivityTaskBinding;
import com.example.projectmanagement.ui.adapter.FileAttachmentAdapter;
import com.example.projectmanagement.ui.adapter.ImageAttachmentAdapter;
import com.example.projectmanagement.ui.task.vm.TaskViewModel;
import com.example.projectmanagement.viewmodel.AvatarView;

public class TaskActivity extends AppCompatActivity {
    private ActivityTaskBinding binding;
    private TaskViewModel viewModel;
    private InputMethodManager imm;
    private ImageAttachmentAdapter imageAdapter;
    private FileAttachmentAdapter fileAdapter;
    private ActivityResultLauncher<String[]> imagePickerLauncher;
    private ActivityResultLauncher<String[]> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        
        initEdgeToEdge();
        setupToolbar();
        initAdapters();
        registerPickers();
        setupListeners();
        setupObservers();

        hideImageSection();
        hideFileSection();
        hideCmtSection();

        loadSampleData();
        loadComments();
        toggleCmtSection();

        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(binding.etDescription, InputMethodManager.SHOW_IMPLICIT);

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
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View focused = getCurrentFocus();
            if (focused instanceof EditText) {
                Rect outRect = new Rect();
                focused.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)ev.getRawX(), (int)ev.getRawY())) {
                    focused.clearFocus();
                    imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
                    binding.confirmBar.setVisibility(View.GONE);
                    binding.commentBar.setVisibility(View.VISIBLE);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void loadComments() {
        List<Comment> commentList = new ArrayList<>();
        commentList.add(new Comment(1, "Hello mọi người!", 123, 10, new Date()));
        commentList.add(new Comment(2, "Đã xong bước này.", 123, 11, new Date()));
        commentList.add(new Comment(3, "Cần bổ sung thêm phần X.", 123, 12, new Date()));
        viewModel.updateComments(commentList);
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

            tvText.setText(c.getContent());
            tvTime.setText(fmt.format(c.getCreateAt()));
            tvName.setText("Nguyễn Thành Trung");
            avatar.setName("Nguyễn Thành Trung");

            btnOpt.setOnClickListener(v -> showCommentOptions(v, c));
            binding.commentContainer.addView(item);
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
            if(item.getItemId() == R.id.action_edit_comment){
                editComment(c);
                return true;
            }else if(item.getItemId() == R.id.action_delete_comment){
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
        imageAdapter = new ImageAttachmentAdapter(new ArrayList<>(), new ImageAttachmentAdapter.OnAttachmentActionListener() {
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
        fileAdapter = new FileAttachmentAdapter(new ArrayList<>(), new FileAttachmentAdapter.OnAttachmentActionListener() {
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
                            viewModel.addImageUri(uri);
                        }
                        viewModel.toggleImagesExpanded();
                    }
                }
        );

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        for (Uri uri : uris) {
                            String name = "File_" + (viewModel.getFiles().getValue() != null ? viewModel.getFiles().getValue().size() + 1 : 1);
                            String ext = DocumentsContract.getDocumentId(uri).contains(".pdf")
                                    ? "pdf" : "bin";
                            File file = new File(
                                viewModel.getFiles().getValue() != null ? viewModel.getFiles().getValue().size() + 1 : 1,
                                name,
                                uri.toString(),
                                0,
                                ext,
                                1,
                                1
                            );
                            viewModel.addFile(file);
                        }
                        viewModel.toggleFilesExpanded();
                    }
                }
        );
    }

    private void setupListeners() {
        binding.rowMember.setOnClickListener(v -> showToast("Thành viên"));
        binding.rowStartDate.setOnClickListener(v -> showToast("Ngày bắt đầu"));
        binding.rowDueDate.setOnClickListener(v -> showToast("Ngày hết hạn"));

        binding.rowImageAttachments.setOnClickListener(v -> viewModel.toggleImagesExpanded());
        binding.rowFileAttachments.setOnClickListener(v -> viewModel.toggleFilesExpanded());
        binding.rowComments.setOnClickListener(v -> viewModel.toggleCommentsExpanded());
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
            } catch (Exception ignored) {}
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
                        (dlg, w) -> { if (w == 0) imagePickerLauncher.launch(new String[]{"image/*"}); }
                ).show();
    }

    private void showFileUploadDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Upload file")
                .setItems(new String[]{getString(R.string.upload), getString(R.string.cancel)},
                        (dlg, w) -> { if (w == 0) filePickerLauncher.launch(new String[]{"*/*"}); }
                ).show();
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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
        sampleFiles.add(new File(1,"Report.pdf","content://com.example.yourapp/files/Report.pdf",2300,"pdf",1,1));
        sampleFiles.add(new File(2,"Note.txt","content://com.example.yourapp/files/Note.txt",121,"txt",1,1));
        sampleFiles.add(new File(3,"Report.pdf","content://com.example.yourapp/files/Report.pdf",2300,"pdf",1,1));
        sampleFiles.add(new File(4,"Note.txt","content://com.example.yourapp/files/Note.txt",121,"txt",1,1));
        sampleFiles.add(new File(5,"Report.pdf","content://com.example.yourapp/files/Report.pdf",2300,"pdf",1,1));
        sampleFiles.add(new File(6,"Note.txt","content://com.example.yourapp/files/Note.txt",121,"txt",1,1));
        viewModel.updateFiles(sampleFiles);

        // Show sections
        viewModel.toggleImagesExpanded();
        viewModel.toggleFilesExpanded();
    }
}
