package com.example.projectmanagement.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.User;
import com.example.projectmanagement.viewmodel.AvatarView;

import java.util.ArrayList;
import java.util.List;

public class TaskMemberAdapter extends RecyclerView.Adapter<TaskMemberAdapter.MemberViewHolder> {
    private List<User> members;
    private List<User> originalMembers;
    private int selectedPosition = -1;
    private OnMemberSelectedListener listener;

    public interface OnMemberSelectedListener {
        void onMemberSelected(User member);
    }

    public TaskMemberAdapter(List<User> members, OnMemberSelectedListener listener) {
        this.originalMembers = new ArrayList<>(members);
        this.members = new ArrayList<>(members);
        this.listener = listener;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        User member = members.get(position);
        holder.bind(member, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public void selectMember(int position) {
        int previousSelected = selectedPosition;
        selectedPosition = position;
        
        if (previousSelected != -1) {
            notifyItemChanged(previousSelected);
        }
        notifyItemChanged(selectedPosition);
        
        if (listener != null) {
            listener.onMemberSelected(members.get(position));
        }
        
        // Move selected member to top
        if (position > 0) {
            User selectedMember = members.remove(position);
            members.add(0, selectedMember);
            selectedPosition = 0;
            notifyDataSetChanged();
        }
    }

    public User getSelectedMember() {
        return selectedPosition != -1 ? members.get(selectedPosition) : null;
    }

    public int getPositionById(int userId) {
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getId() == userId) {
                return i;
            }
        }
        return -1;
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final ImageView ivCheck;
        private final AvatarView ivAvatar;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            ivCheck = itemView.findViewById(R.id.iv_check);
            ivAvatar = itemView.findViewById(R.id.avatar_member_task);
        }

        public void bind(User member, boolean isSelected) {
            tvName.setText(member.getFullname());
            ivCheck.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            
            // Set color for check icon
            if (isSelected) {
                ivCheck.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.colorAccent));
            }
            
            // Set avatar or first letter of name
            if (member.getAvatar() != null && !member.getAvatar().isEmpty()) {
                ivAvatar.setImage(android.net.Uri.parse(member.getAvatar()));
            } else {
                ivAvatar.setName(member.getFullname());
            }

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    selectMember(position);
                }
            });
        }
    }
} 