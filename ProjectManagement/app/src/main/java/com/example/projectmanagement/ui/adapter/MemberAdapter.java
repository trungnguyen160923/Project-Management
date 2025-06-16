package com.example.projectmanagement.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.ProjectMember;
import com.example.projectmanagement.data.model.User;
import com.example.projectmanagement.utils.UserPreferences;
import com.example.projectmanagement.viewmodel.AvatarView;

import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {
    private final List<ProjectMember> memberList;
    private final Context context;
    private final String currentUserEmail;
    private boolean isCurrentUserAdmin;
    private OnRoleChangeListener roleChangeListener;
    private OnRemoveMemberListener removeMemberListener;

    public interface OnRoleChangeListener {
        void onRoleChanged(ProjectMember member, ProjectMember.Role newRole);
    }

    public interface OnRemoveMemberListener {
        void onRemoveMember(ProjectMember member);
    }

    public MemberAdapter(List<ProjectMember> memberList, Context context) {
        this.memberList = memberList;
        this.context = context;
        UserPreferences userPreferences = new UserPreferences(context);
        User currentUser = userPreferences.getUser();
        Log.d("MemberAdapter", "Current user from preferences: " + 
            (currentUser != null ? currentUser.getEmail() : "null"));
        this.currentUserEmail = currentUser != null ? currentUser.getEmail() : "";
        updateAdminStatus();
    }

    private void updateAdminStatus() {
        if (memberList == null || memberList.isEmpty()) {
            Log.d("MemberAdapter", "Members list is null or empty");
            this.isCurrentUserAdmin = false;
            return;
        }

        for (ProjectMember member : memberList) {
            if (member.getUser() != null && 
                member.getUser().getEmail().equals(currentUserEmail)) {
                this.isCurrentUserAdmin = member.getRole() == ProjectMember.Role.Admin;
                Log.d("MemberAdapter", "Found current user, isAdmin: " + isCurrentUserAdmin);
                return;
            }
        }
        Log.d("MemberAdapter", "Current user not found in members list");
        this.isCurrentUserAdmin = false;
    }

    public void setOnRemoveMemberListener(OnRemoveMemberListener listener) {
        this.removeMemberListener = listener;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        // Update admin status before binding
        updateAdminStatus();
        
        ProjectMember member = memberList.get(position);
        User user = member.getUser();

        // Avatar
        if (user != null && user.getAvatar() != null && !user.getAvatar().equals("img/avatar/default.png")) {
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

        // Role handling
        boolean isCurrentMember = user != null && user.getEmail().equals(currentUserEmail);
        boolean canChangeRole = isCurrentUserAdmin && !isCurrentMember && member.getRole() != ProjectMember.Role.Admin;

        Log.d("MemberAdapter", "Position: " + position + 
            ", User: " + (user != null ? user.getEmail() : "null") +
            ", isCurrentUserAdmin: " + isCurrentUserAdmin +
            ", isCurrentMember: " + isCurrentMember +
            ", memberRole: " + member.getRole() +
            ", canChangeRole: " + canChangeRole);

        if (canChangeRole) {
            // Show role text and menu button
            holder.tvRole.setVisibility(View.VISIBLE);
            holder.btnRoleMenu.setVisibility(View.VISIBLE);
            
            // Set current role text
            String roleText = member.getRole() == ProjectMember.Role.Leader ? "Trưởng nhóm" : "Thành viên";
            holder.tvRole.setText(roleText);
            
            // Setup PopupMenu
            holder.btnRoleMenu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(context, holder.btnRoleMenu);
                
                // Add role options
                popup.getMenu().add("Trưởng nhóm");
                popup.getMenu().add("Thành viên");
                
                // Add separator
                popup.getMenu().add(Menu.NONE, Menu.NONE, Menu.NONE, "----------------");
                
                // Add remove option with red color
                SpannableString removeText = new SpannableString("Buộc rời");
                removeText.setSpan(new ForegroundColorSpan(Color.RED), 0, removeText.length(), 0);
                popup.getMenu().add(removeText);
                
                popup.setOnMenuItemClickListener(item -> {
                    String selectedRole = item.getTitle().toString();
                    
                    if (selectedRole.equals("Buộc rời")) {
                        if (removeMemberListener != null) {
                            removeMemberListener.onRemoveMember(member);
                        }
                        return true;
                    }
                    
                    ProjectMember.Role newRole = selectedRole.equals("Trưởng nhóm") ? 
                        ProjectMember.Role.Leader : ProjectMember.Role.Member;
                    
                    if (roleChangeListener != null && newRole != member.getRole()) {
                        roleChangeListener.onRoleChanged(member, newRole);
                    }
                    return true;
                });
                
                popup.show();
            });
        } else {
            // Show text for non-admin users or for admin's own role
            holder.tvRole.setVisibility(View.VISIBLE);
            holder.btnRoleMenu.setVisibility(View.GONE);
            
            String roleText = "";
            switch (member.getRole()) {
                case Admin:
                    roleText = "Quản trị viên";
                    break;
                case Leader:
                    roleText = "Trưởng nhóm";
                    break;
                case Member:
                    roleText = "Thành viên";
                    break;
            }
            holder.tvRole.setText(roleText);
        }
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    public void setOnRoleChangeListener(OnRoleChangeListener listener) {
        this.roleChangeListener = listener;
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        public AvatarView avatarView;
        public TextView tvName, tvEmail, tvRole;
        public ImageView btnRoleMenu;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvRole = itemView.findViewById(R.id.tv_role);
            btnRoleMenu = itemView.findViewById(R.id.btn_role_menu);
        }
    }
}