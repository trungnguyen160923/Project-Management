package com.example.projectmanagement.ui.project;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.data.model.ProjectHolder;
import com.example.projectmanagement.data.model.ProjectMember;
import com.example.projectmanagement.databinding.ActivityMenuProjectBinding;
import com.example.projectmanagement.ui.main.HomeActivity;
import com.example.projectmanagement.ui.project.vm.MenuProjectViewModel;
import com.example.projectmanagement.utils.ConfirmDialogUtil;
import com.example.projectmanagement.viewmodel.AvatarView;
import com.example.projectmanagement.utils.ParseDateUtil;
import com.example.projectmanagement.data.repository.ProjectRepository;
import com.example.projectmanagement.utils.LoadingDialog;

import java.util.Date;

public class MenuProjectActivity extends AppCompatActivity {
    private String TAG = "MenuProjectActivity";
    private ActivityMenuProjectBinding binding;
    private MenuProjectViewModel viewModel;
    private Project project;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMenuProjectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MenuProjectViewModel.class);
        viewModel.init(this);
        
        project = ProjectHolder.get();
        if (project == null) {
            Toast.makeText(this, "Không nhận được Project", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set project vào ViewModel
        viewModel.setProject(project);

        // Log để debug
        Log.d("MenuProjectActivity", "Project: " + project.toString());
        Log.d("MenuProjectActivity", "Description: " + project.getProjectDescription());
        Log.d("MenuProjectActivity", "Deadline: " + project.getDeadline());

        loadingDialog = new LoadingDialog(this);

        observeData();
        setupToolbar();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh project data when returning from UpdateProjectActivity
        Project currentProject = ProjectHolder.get();
        if (currentProject != null) {
            viewModel.setProject(currentProject);
        }
    }

    private void observeData() {
        viewModel.getProjectLive().observe(this, project -> {
            if (project != null) {
                Log.d("MenuProjectActivity", "Project in ViewModel: " + project.toString());

                // Hiển thị thông tin project
                String description = project.getProjectDescription();
                Log.d("MenuProjectActivity", "Setting description: " + description);
                if (description != null && !description.isEmpty()) {
                    binding.tvDescription.setText(description);
                } else {
                    binding.tvDescription.setText("Chưa có mô tả");
                }

                // Hiển thị thời hạn
                if (project.getDeadline() != null) {
                    String deadlineText = String.format("Từ %s đến %s",
                            ParseDateUtil.toCustomDateTime((project.getStartDate())),
                            ParseDateUtil.toCustomDateTime(project.getDeadline()));
                    Log.d("MenuProjectActivity", "Setting deadline: " + deadlineText);
                    binding.tvDeadline.setText(deadlineText);
                } else {
                    binding.tvDeadline.setText("Chưa có thời hạn");
                }
            } else {
                Log.d("MenuProjectActivity", "Project in ViewModel is null");
            }
        });

        viewModel.getMemberListLive().observe(this, memberList -> {
            if (memberList != null) {
                // Tìm và hiển thị thông tin admin
                for (ProjectMember member : memberList) {
                    if (member.getRole() == ProjectMember.Role.Admin) {
                        // Set avatar
                        if (member.getUser() != null && member.getUser().getAvatar() != null
                        && !member.getUser().getAvatar().equals("img/avatar/default.png")) {
                            binding.avatarView.setImage(Uri.parse(member.getUser().getAvatar()));
                        } else {
                            binding.avatarView.setName(member.getUser().getFullname());
                        }

                        // Set tên và email
                        binding.tvAuthorName.setText(member.getUser().getFullname());
                        binding.tvAuthorUsername.setText(member.getUser().getEmail());
                        break;
                    }
                }

                // Thêm avatar cho từng thành viên
                for (ProjectMember member : memberList) {
                    Log.d("MenuProjectActivity", "Member: " + member.toString());
                    Log.d("MenuProjectActivity", "User: " + (member.getUser() != null ? member.getUser().toString() : "null"));

                    AvatarView avatarView = new AvatarView(this, null);

                    // Thiết lập layout params cho avatar
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            getResources().getDimensionPixelSize(R.dimen.avatar_size),
                            getResources().getDimensionPixelSize(R.dimen.avatar_size)
                    );
                    params.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.avatar_margin));
                    avatarView.setLayoutParams(params);

                    // Nếu có avatar thì set ảnh, không thì hiển thị chữ cái đầu
                    if (member.getUser() != null && member.getUser().getAvatar() != null) {
                        Log.d("MenuProjectActivity", "Setting avatar for: " + member.getUser().getFullname());
                        avatarView.setImage(Uri.parse(member.getUser().getAvatar()));
                    } else {
                        String fullname = member.getUser() != null ? member.getUser().getFullname() : "Unknown";
                        Log.d("MenuProjectActivity", "Setting initials for: " + fullname);
                        avatarView.setName(fullname);
                    }

                    // Thêm avatar vào layout
//                    binding.layoutAvatarList.addView(avatarView);
                }
            }
        });
    }


    private void setupToolbar() {
        setSupportActionBar(binding.toolbarMenuProject);
        binding.toolbarMenuProject.setNavigationOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        binding.members.setOnClickListener(v -> {
            // Nếu muốn truyền projectId sang MembersActivity:
            Intent intent = new Intent(this, MembersActivity.class);
            Log.d(TAG, ">>> click thanh vien: " + project);
            intent.putExtra("project", project);
            startActivity(intent);
        });

        binding.btnInvite.setOnClickListener(v -> {
            // Nếu muốn truyền projectId sang InviteMemberActivity:
            Intent intent = new Intent(this, InviteMemberActivity.class);
            startActivity(intent);
        });

        binding.btnDeleteProject.setOnClickListener(v -> {
            ConfirmDialogUtil.show(
                    this,
                    "Xác nhận xoá", // title
                    "Bạn có chắc chắn muốn xoá project này không? Hành động này không thể hoàn tác.", // message
                    "Xoá", // textConfirm
                    "Huỷ", // textCancel
                    R.drawable.ic_warning, // iconResId
                    () -> {
                        // Hiển thị loading
                        loadingDialog.show();

                        // Gọi API xóa project
                        Project currentProject = ProjectHolder.get();
                        if (currentProject != null) {
                            ProjectRepository.getInstance(this).deleteProject(currentProject.getProjectID())
                                    .observe(this, success -> {
                                        loadingDialog.dismiss();
                                        Log.d("KKKKKKKKK", success.toString());
                                        if (success) {
                                            // Xóa thành công
                                            Toast.makeText(this, "Đã xoá project thành công!", Toast.LENGTH_SHORT).show();

                                            // Chuyển về màn hình danh sách project
                                            Intent intent = new Intent(this, HomeActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            // Xóa thất bại
                                            Toast.makeText(this, "Không thể xoá project", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            loadingDialog.dismiss();
                            Toast.makeText(this, "Không tìm thấy project", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        });

        binding.btnUpdateProject.setOnClickListener(v -> {
            Intent intent = new Intent(this, UpdateProjectActivity.class);
            startActivity(intent);
        });
    }


}