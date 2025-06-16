package com.example.projectmanagement.ui.project;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
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
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.data.model.ProjectHolder;
import com.example.projectmanagement.data.model.ProjectMember;
import com.example.projectmanagement.data.model.User;
import com.example.projectmanagement.databinding.ActivityInviteMemberBinding;
import com.example.projectmanagement.ui.adapter.MemberAdapter;
import com.example.projectmanagement.ui.project.vm.MembersViewModel;
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
    private MembersViewModel viewModel;

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
        String toolbarTitle = "Thành viên của " + (projectName != null && !projectName.isEmpty() ? " " + projectName : "");
        binding.toolbarInvite.setTitle(toolbarTitle);
        setSupportActionBar(binding.toolbarInvite);
        // Xử lý nút X (navigation icon)
        binding.toolbarInvite.setNavigationOnClickListener(v -> finish());

        binding.tvProjectName.setText(projectName);

        // Khởi tạo adapter
        memberAdapter = new MemberAdapter(filteredMembers, this);
        binding.rvMembers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMembers.setAdapter(memberAdapter);

        // Set role change listener
        memberAdapter.setOnRoleChangeListener((member, newRole) -> {
            viewModel.updateMemberRole(member, newRole);
        });

        // Set remove member listener
        memberAdapter.setOnRemoveMemberListener(member -> {
            // Show confirmation dialog
            new AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc chắn muốn buộc thành viên này rời khỏi dự án?")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    viewModel.removeMember(member);
                })
                .setNegativeButton("Hủy", null)
                .show();
        });

        // Khởi tạo ViewModel và observe danh sách thành viên
        viewModel = new ViewModelProvider(this).get(MembersViewModel.class);
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
