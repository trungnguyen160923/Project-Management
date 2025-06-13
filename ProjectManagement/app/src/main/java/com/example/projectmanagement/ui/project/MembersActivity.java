package com.example.projectmanagement.ui.project;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.data.model.ProjectHolder;
import com.example.projectmanagement.data.model.ProjectMember;
import com.example.projectmanagement.data.model.User;
import com.example.projectmanagement.ui.adapter.MemberAdapter;
import com.example.projectmanagement.databinding.ActivityInviteMemberBinding;
import com.example.projectmanagement.ui.project.vm.InviteMemberViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class MembersActivity extends AppCompatActivity {
    private String TAG = "MembersActivity";
    private ActivityInviteMemberBinding binding;
    private MemberAdapter memberAdapter;
    private List<ProjectMember> allMembers = new ArrayList<>();
    private List<ProjectMember> filteredMembers = new ArrayList<>();
    private String inviteLink = "";
    private String projectName = "";
    private InviteMemberViewModel viewModel;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        memberAdapter = new MemberAdapter(filteredMembers, this);
        binding.rvMembers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMembers.setAdapter(memberAdapter);
        // Khởi tạo ViewModel và observe danh sách thành viên
        viewModel = new ViewModelProvider(this).get(InviteMemberViewModel.class);
        Project project = (Project) getIntent().getParcelableExtra("project");
        viewModel.setProjectData(project);
        viewModel.init(this);
        viewModel.getMemberListLive().observe(this, members -> {
            filteredMembers.clear();
            filteredMembers.addAll(members);
            memberAdapter.notifyDataSetChanged();
        });

        // Tìm kiếm thành viên
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterMembers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterMembers(newText);
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

    private void filterMembers(String query) {
        filteredMembers.clear();
        List<ProjectMember> allMembers = viewModel.getMemberListLive().getValue();
        if (allMembers == null) return;
        if (query == null || query.isEmpty()) {
            filteredMembers.addAll(allMembers);
        } else {
            for (ProjectMember member : allMembers) {
                User user = member.getUser();
                if (user != null && (user.getFullname().toLowerCase().contains(query.toLowerCase())
                        || user.getEmail().toLowerCase().contains(query.toLowerCase())
                        || user.getUsername().toLowerCase().contains(query.toLowerCase()))) {
                    filteredMembers.add(member);
                }
            }
        }
        memberAdapter.notifyDataSetChanged();
    }
}
