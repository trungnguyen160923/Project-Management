package com.example.projectmanagement.ui.adapter;

import static android.view.View.INVISIBLE;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.data.model.ProjectMember;
import com.example.projectmanagement.data.model.User;
import com.example.projectmanagement.data.service.ProjectMemberService;
import com.example.projectmanagement.utils.Helpers;
import com.example.projectmanagement.viewmodel.AvatarView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MemberViewHolder> {
    private String TAG = "UserAdapter";
    private final List<User> userList;
    private final Context context;
    private final String currentUserEmail; // Để phân biệt (bạn)
    private Project project;

    public UserAdapter(List<User> userList, Context context, Project project) {
        Log.d(TAG, ">>> pro pro pro: " + project);
        this.project = project;
        this.userList = userList;
        this.context = context;
        // Lấy email user hiện tại (giả lập, bạn có thể lấy từ SharedPreferences hoặc UserHolder)
        this.currentUserEmail = "2imtrung160923@gmail.com";
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        User user = userList.get(position);

        // Avatar
        if (user != null && user.getAvatar() != null) {
            holder.avatarView.setImage(android.net.Uri.parse(user.getAvatar()));
        } else {
            holder.avatarView.setName(user != null ? user.getFullname() : "?");
        }

        // Họ tên (nếu là bạn thì thêm "(bạn)")
        String name = user != null ? user.getFullname() : "Unknown";
        if (user != null && user.getEmail() != null && user.getEmail().equalsIgnoreCase(currentUserEmail)) {
            name += " (bạn)";
        }
        holder.tvName.setText(name);

        // Email hoặc username
        holder.tvEmail.setText(user != null ? user.getEmail() : "");

        holder.tvRole.setVisibility(INVISIBLE);

        // 👇 Bắt sự kiện click vào item
        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, ">>> tap on member");
            List<Integer> userIDs = List.of(user.getId());
            ProjectMemberService.sendProjectInvitation(context, project.getProjectID(), userIDs, res -> {
                Toast.makeText(context, "Đã gửi lời mời thành công!", android.widget.Toast.LENGTH_SHORT).show();
            }, err -> {
                String errorMessage = err.getMessage();
                try {
                    errorMessage = Helpers.parseError(err);
                } catch (Exception e) {
                }
                Toast.makeText(context, errorMessage, android.widget.Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        public AvatarView avatarView;
        public TextView tvName, tvEmail, tvRole;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvRole = itemView.findViewById(R.id.tv_role);
        }
    }
}