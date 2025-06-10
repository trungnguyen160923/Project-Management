package com.example.projectmanagement.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.ProjectMember;
import com.example.projectmanagement.data.model.User;
import com.example.projectmanagement.viewmodel.AvatarView;

import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {
    private final List<ProjectMember> memberList;
    private final Context context;
    private final String currentUserEmail; // Để phân biệt (bạn)

    public MemberAdapter(List<ProjectMember> memberList, Context context) {
        this.memberList = memberList;
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
        ProjectMember member = memberList.get(position);
        User user = member.getUser();

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

        // Role
        String roleText = "";
        switch (member.getRole()) {
            case ADMIN:
                roleText = "Quản trị viên";
                break;
            case LEADER:
                roleText = "Trưởng nhóm";
                break;
            case MEMBER:
                roleText = "Thành viên";
                break;
        }
        holder.tvRole.setText(roleText);
    }

    @Override
    public int getItemCount() {
        return memberList.size();
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