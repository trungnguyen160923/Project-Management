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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.Comment;
import com.example.projectmanagement.data.model.File;
import com.example.projectmanagement.ui.adapter.FileAttachmentAdapter;
import com.example.projectmanagement.ui.adapter.ImageAttachmentAdapter;
import com.example.projectmanagement.viewmodel.AvatarView;

public class TaskActivity extends AppCompatActivity {
    // Detail items
    private View rowMember, rowStartDate, rowDueDate, rowCmts;

    // Image attachments
    private View rowImageAttachments, separatorImage;
    private RecyclerView rvImageAttachments;
    private ImageView ivExpandImages;

    // File attachments
    private View rowFileAttachments, separatorFile;
    private RecyclerView rvFileAttachments;
    private ImageView ivExpandFiles;

    // Comments
    private LinearLayout commentContainer;
    private ImageView ivExpandComments;

    // Data
    private final List<Uri> imageUris = new ArrayList<>();
    private final List<File> files = new ArrayList<>();
    private List<Comment> commentList;

    // State
    private boolean isImagesExpanded;
    private boolean isFilesExpanded;
    private boolean isCmtExpanded;

    // Pickers
    private ActivityResultLauncher<String[]> imagePickerLauncher;
    private ActivityResultLauncher<String[]> filePickerLauncher;

    // Adapters
    private ImageAttachmentAdapter imageAdapter;
    private FileAttachmentAdapter fileAdapter;
    private AvatarView avatarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        initEdgeToEdge();
        setupToolbar();
        bindViews();
        initAdapters();
        registerPickers();
        setupListeners();

        hideImageSection();
        hideFileSection();
        hideCmtSection();

        loadSampleData();
        loadComments();
        // Initially, only comments if present
        toggleCmtSection();

//        EditText et = findViewById(R.id.et_description);
//        LinearLayout commentBar = findViewById(R.id.comment_bar);
//        final View root = findViewById(android.R.id.content);
//        et.requestFocus();
//        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
//        View confirmBar = findViewById(R.id.confirm_bar);
//
//        et.setOnFocusChangeListener((v, hasFocus) -> {
//            confirmBar.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
//            commentBar.setVisibility(View.GONE);
//        });
//        root.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
//            Rect r = new Rect();
//            root.getWindowVisibleDisplayFrame(r);
//            int screenHeight = root.getRootView().getHeight();
//            int keypadHeight = screenHeight - r.bottom;
//
//            // Nếu bàn phím chiếm > 15% màn hình thì coi như đã bật
//            if (keypadHeight > screenHeight * 0.15) {
//                // Đẩy confirm_bar lên trên keyboard
//                confirmBar.setTranslationY(-keypadHeight);
//            } else {
//                // Trả nó về đáy
//                confirmBar.setTranslationY(0);
//            }
//        });

    }

    private void loadComments() {
        commentList = new ArrayList<>();
        commentList.add(new Comment(1, "Hello mọi người!", 123, 10, new Date()));
        commentList.add(new Comment(2, "Đã xong bước này.", 123, 11, new Date()));
        commentList.add(new Comment(3, "Cần bổ sung thêm phần X.", 123, 12, new Date()));
    }

    private void showAllComments() {
        if (commentList == null || commentList.isEmpty()) return;
        commentContainer.setVisibility(View.VISIBLE);
        commentContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);
        SimpleDateFormat fmt = new SimpleDateFormat("d MMM yyyy 'lúc' HH:mm", Locale.getDefault());

        for (Comment c : commentList) {
            View item = inflater.inflate(R.layout.item_comment, commentContainer, false);
            AvatarView avatar = item.findViewById(R.id.ivCommentAvatar);
            TextView tvName  = item.findViewById(R.id.tvCommentName);
            TextView tvTime  = item.findViewById(R.id.tvCommentTime);
            TextView tvText  = item.findViewById(R.id.tvCommentText);
            ImageButton btnOpt = item.findViewById(R.id.btnCommentOptions);

            tvText.setText(c.getContent());
            tvTime.setText(fmt.format(c.getCreateAt()));
            tvName.setText("Nguyễn Thành Trung");
            avatar.setName("Nguyễn Thành Trung");

            btnOpt.setOnClickListener(v -> showCommentOptions(v,c));
            commentContainer.addView(item);
        }

        isCmtExpanded = true;
        ivExpandComments.setImageResource(R.drawable.ic_expand_less);
    }

    private void hideCmtSection() {
        commentContainer.removeAllViews();
        commentContainer.setVisibility(View.GONE);
        isCmtExpanded = false;
        ivExpandComments.setImageResource(R.drawable.ic_expand_more);
    }

    private void toggleCmtSection() {
        if (commentList == null || commentList.isEmpty()) {
            hideCmtSection();
        } else if (isCmtExpanded) {
            hideCmtSection();
        } else {
            showAllComments();
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
        // Hiển thị dialog chỉnh sửa, cập nhật dữ liệu và gọi showAllComments() lại
    }

    private void deleteComment(Comment c) {
        // Xóa khỏi danh sách, notify UI
        commentList.remove(c);
        showAllComments();
    }

    private void initEdgeToEdge() {
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main),
                (view, insets) -> {
                    Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    view.setPadding(sys.left, sys.top, sys.right, sys.bottom);
                    return insets;
                }
        );
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_task);
        setSupportActionBar(toolbar);
        Drawable nav = toolbar.getNavigationIcon();
        if (nav != null) {
            nav.setTint(ContextCompat.getColor(this, R.color.black));
            toolbar.setNavigationIcon(nav);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void bindViews() {
        rowMember = findViewById(R.id.row_member);
        rowStartDate = findViewById(R.id.row_start_date);
        rowDueDate = findViewById(R.id.row_due_date);
        rowCmts = findViewById(R.id.row_comments);

        rowImageAttachments = findViewById(R.id.row_image_attachments);
        separatorImage = findViewById(R.id.separator_image);
        rvImageAttachments = findViewById(R.id.rv_image_attachments);
        ivExpandImages = findViewById(R.id.iv_expand_images);

        rowFileAttachments = findViewById(R.id.row_file_attachments);
        separatorFile = findViewById(R.id.separator_file);
        rvFileAttachments = findViewById(R.id.rv_file_attachments);
        ivExpandFiles = findViewById(R.id.iv_expand_files);

        commentContainer = findViewById(R.id.commentContainer);
        ivExpandComments = findViewById(R.id.iv_expand_comments);

        avatarView = findViewById(R.id.img_avatar);
    }

    private void initAdapters() {
        rvImageAttachments.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        rvImageAttachments.setNestedScrollingEnabled(false);
        imageAdapter = new ImageAttachmentAdapter(imageUris, new ImageAttachmentAdapter.OnAttachmentActionListener() {
            @Override
            public void onDownloadClicked(int position) {
                Uri uri = imageUris.get(position);
                // TODO: xử lý tải xuống ảnh
            }
            @Override
            public void onDeleteClicked(int position) {
                imageUris.remove(position);
                imageAdapter.notifyItemRemoved(position);
            }
        });
        rvImageAttachments.setAdapter(imageAdapter);

        rvFileAttachments.setLayoutManager(
                new LinearLayoutManager(this)
        );
        rvFileAttachments.setNestedScrollingEnabled(false);
        fileAdapter = new FileAttachmentAdapter(files, new FileAttachmentAdapter.OnAttachmentActionListener() {
            @Override
            public void onDownloadClicked(int position) {
//                Uri uri = files.get(position).getUri();
                // TODO: download logic
            }
            @Override
            public void onDeleteClicked(int position) {
                files.remove(position);
                fileAdapter.notifyItemRemoved(position);
            }
        });
        rvFileAttachments.setAdapter(fileAdapter);
    }

    private void registerPickers() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        imageUris.addAll(uris);
                        toggleImageSection();
                    }
                }
        );

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        for (Uri uri : uris) {
                            String name = "File_" + (files.size() + 1);
                            String ext = DocumentsContract.getDocumentId(uri).contains(".pdf")
                                    ? "pdf" : "bin";
                            String size = "?? KB";
//                            files.add(new File(name, ext, size, uri));
                        }
                        toggleFileSection();
                    }
                }
        );
    }

    private void setupListeners() {
        rowMember.setOnClickListener(v -> showToast("Thành viên"));
        rowStartDate.setOnClickListener(v -> showToast("Ngày bắt đầu"));
        rowDueDate.setOnClickListener(v -> showToast("Ngày hết hạn"));

        rowImageAttachments.setOnClickListener(v -> toggleImageSection());
        rowFileAttachments.setOnClickListener(v -> toggleFileSection());
        rowCmts.setOnClickListener(v -> toggleCmtSection());
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
        rowImageAttachments.setVisibility(View.VISIBLE);
        separatorImage.setVisibility(View.VISIBLE);
        rvImageAttachments.setVisibility(View.VISIBLE);
        ivExpandImages.setImageResource(R.drawable.ic_expand_less);
        isImagesExpanded = true;
        imageAdapter.notifyDataSetChanged();
    }

    private void hideImageSection() {
//        rowImageAttachments.setVisibility(View.GONE);
        separatorImage.setVisibility(View.GONE);
        rvImageAttachments.setVisibility(View.GONE);
        ivExpandImages.setImageResource(R.drawable.ic_expand_more);
        isImagesExpanded = false;
//        imageUris.clear();
    }

    private void toggleImageSection() {
        if (isImagesExpanded) hideImageSection();
        else                   showImageSection();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showFileSection() {
        rowFileAttachments.setVisibility(View.VISIBLE);
        separatorFile.setVisibility(View.VISIBLE);
        rvFileAttachments.setVisibility(View.VISIBLE);
        ivExpandFiles.setImageResource(R.drawable.ic_expand_less);
        isFilesExpanded = true;
        fileAdapter.notifyDataSetChanged();
    }

    private void hideFileSection() {
//        rowFileAttachments.setVisibility(View.GONE);
        separatorFile.setVisibility(View.GONE);
        rvFileAttachments.setVisibility(View.GONE);
        ivExpandFiles.setImageResource(R.drawable.ic_expand_more);
        isFilesExpanded = false;
//        files.clear();
    }

    private void toggleFileSection() {
        if (isFilesExpanded) hideFileSection();
        else                 showFileSection();
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
        //        if (user.hasPhoto()) {
//            avatarView.setImage(user.getPhotoUri());
//        } else {
//            avatarView.setName(user.getFullName());
//        }
        avatarView.setName("Nguyễn Thành Trung");
        // giả lập vài URI từ drawable
        imageUris.add(Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.calendar));
        imageUris.add(Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.clock));
        imageUris.add(Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.calendar));
        imageUris.add(Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.calendar));
        imageUris.add(Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.clock));
        imageUris.add(Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.calendar));
        files.add(new File(1,"Report.pdf","content://com.example.yourapp/files/Report.pdf",2300,"pdf",1,1));
        files.add(new File(2,"Note.txt","content://com.example.yourapp/files/Note.txt",121,"txt",1,1 ));
        files.add(new File(3,"Report.pdf","content://com.example.yourapp/files/Report.pdf",2300,"pdf",1,1));
        files.add(new File(4,"Note.txt","content://com.example.yourapp/files/Note.txt",121,"txt",1,1 ));
        files.add(new File(5,"Report.pdf","content://com.example.yourapp/files/Report.pdf",2300,"pdf",1,1));
        files.add(new File(6,"Note.txt","content://com.example.yourapp/files/Note.txt",121,"txt",1,1 ));

        // hiển thị section
        showImageSection();
        showFileSection();
    }
}
