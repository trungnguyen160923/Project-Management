package com.example.projectmanagement.ui.project;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.convertor.UserConvertor;
import com.example.projectmanagement.data.model.Notification;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.data.model.ProjectHolder;
import com.example.projectmanagement.data.model.ProjectMember;
import com.example.projectmanagement.data.model.User;
import com.example.projectmanagement.data.service.UserService;
import com.example.projectmanagement.databinding.ActivityInviteMemberBinding;
import com.example.projectmanagement.ui.adapter.UserAdapter;
import com.example.projectmanagement.ui.project.vm.InviteMemberViewModel;
import com.example.projectmanagement.ui.project.vm.MembersViewModel;
import com.example.projectmanagement.utils.Helpers;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InviteMemberActivity extends AppCompatActivity {
    private String TAG = "InviteMemberActivity";
    private ActivityInviteMemberBinding binding;
    private UserAdapter userAdapter;
    private List<User> users = new ArrayList<>();
    private String inviteLink = "";
    private String projectName = "";
    private InviteMemberViewModel viewModel;
    private final long DEBOUNCE_DELAY = 300; // 300ms
    private final android.os.Handler handler = new android.os.Handler();
    private Runnable searchRunnable;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, ">>> run Invite member activity");
        super.onCreate(savedInstanceState);
        binding = ActivityInviteMemberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Lấy tên project
        if (ProjectHolder.get() != null) {
            projectName = ProjectHolder.get().getProjectName();
        }
        // Set tiêu đề toolbar: "Mời tham gia <tên project>"
        String toolbarTitle = "Mời tham gia" + (projectName != null && !projectName.isEmpty() ? " " + projectName : "");
        binding.toolbarInvite.setTitle(toolbarTitle);
        setSupportActionBar(binding.toolbarInvite);
        // Xử lý nút X (navigation icon)
        binding.toolbarInvite.setNavigationOnClickListener(v -> finish());

        binding.tvProjectName.setText(projectName);

        // Khởi tạo adapter
        userAdapter = new UserAdapter(users, this,ProjectHolder.get());
        binding.rvMembers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMembers.setAdapter(userAdapter);

        // Khởi tạo ViewModel và observe danh sách thành viên
        viewModel = new ViewModelProvider(this).get(InviteMemberViewModel.class);
        viewModel.getUserListLive().observe(this, users -> {
            users.clear();
            users.addAll(users);
            userAdapter.notifyDataSetChanged();
        });

        // Tìm kiếm thành viên
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Đã có debounce, không cần xử lý ở đây
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchRunnable != null) handler.removeCallbacks(searchRunnable);
                searchRunnable = () -> searchUsers(newText);
                handler.postDelayed(searchRunnable, DEBOUNCE_DELAY);
                return true;
            }
        });


        // Tạo liên kết mời
//        binding.layoutCreateLink.setOnClickListener(v -> {
//            inviteLink = "https://yourapp.com/invite?project=" + projectName;
//            binding.layoutCreateLink.setVisibility(View.GONE);
//            binding.layoutInviteLink.setVisibility(View.VISIBLE);
//        });

        // Copy link
        binding.btnCopyLink.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Invite Link", inviteLink);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Đã copy liên kết!", Toast.LENGTH_SHORT).show();
        });

        // Chọn quyền mời (giả lập, bạn có thể show dialog chọn quyền ở đây)
        binding.tvInvitePermission.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_invite_link_permission, null, false);

            AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomDialogTheme)
                    .setView(dialogView)
                    .create();

            RadioButton rbMember = dialogView.findViewById(R.id.rb_member);
            RadioButton rbObserver = dialogView.findViewById(R.id.rb_observer);
            MaterialButton btnSave = dialogView.findViewById(R.id.btn_save_setting_inviteLink);
            TextView tvDelete = dialogView.findViewById(R.id.btn_delete_inviteLink);

            rbMember.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) rbObserver.setChecked(false);
            });
            rbObserver.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) rbMember.setChecked(false);
            });

            btnSave.setOnClickListener(v2 -> {
                // TODO: Lưu quyền đã chọn, cập nhật UI nếu cần
                dialog.dismiss();
            });
            tvDelete.setOnClickListener(v2 -> {
                // Ẩn layout hiển thị link, hiện layout tạo link lại
                binding.layoutInviteLink.setVisibility(View.GONE);
//                binding.layoutCreateLink.setVisibility(View.VISIBLE);
                // Xoá link cũ (nếu muốn)
                inviteLink = "";
                dialog.dismiss();
            });


            dialog.show();
        });
    }

    private void searchUsers(String query) {
        if (query.isEmpty()) return;
        UserService.searchUsers(this, query, res -> {
            Log.d(TAG, ">>> on success searchUsers: " + res.toString());
            List<User> fetchedUsers = new ArrayList<>();
            JSONArray data = res.optJSONArray("data");
            if (data != null) {
                try {
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject json = data.getJSONObject(i);
                        fetchedUsers.add(UserConvertor.fromJson(json));
                    }
                } catch (Exception e) {
                }
            }
            users.clear();
            users.addAll(fetchedUsers);
            userAdapter.notifyDataSetChanged();
        }, err -> {
            Log.d(TAG, ">>> on error searchUsers: " + err.toString());
            String errMsg = "lỗi không thể tìm kiếm thành viên";
            try {
                errMsg = Helpers.parseError(err);
            } catch (Exception e) {
            }
            Toast.makeText(this, errMsg, Toast.LENGTH_SHORT).show();
        });
    }
}
