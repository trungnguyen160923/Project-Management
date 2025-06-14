package com.example.projectmanagement.ui.adapter;

import android.util.Log;
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
    private OnMemberSelectedListener listener;
    private int selectedPosition = -1;
    private int assignedPosition = -1;
    private boolean itemsEnabled = true;

    public interface OnMemberSelectedListener {
        void onMemberSelected(User member);
    }

    public TaskMemberAdapter(List<User> members, OnMemberSelectedListener listener) {
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
        boolean isSelected = position == selectedPosition;
        boolean isAssigned = position == assignedPosition && selectedPosition == -1;
        holder.bind(member, isSelected, isAssigned);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public void selectMember(int position) {
        if (!itemsEnabled) return;
        
        int oldPosition = selectedPosition;
        selectedPosition = position;
        
        if (oldPosition != -1) {
            notifyItemChanged(oldPosition);
        }
        if (position != -1) {
            notifyItemChanged(position);
        }
        if (assignedPosition != -1) {
            notifyItemChanged(assignedPosition);
        }
        
        if (listener != null) {
            listener.onMemberSelected(position != -1 ? members.get(position) : null);
        }
    }

    public void selectMemberById(int userId) {
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getId() == userId) {
                assignedPosition = i;
                selectMember(i);
                break;
            }
        }
    }

    public void deselectAll() {
        int oldPosition = selectedPosition;
        selectedPosition = -1;
        if (oldPosition != -1) {
            notifyItemChanged(oldPosition);
            if (listener != null) {
                listener.onMemberSelected(null);
            }
        }
        if (assignedPosition != -1) {
            notifyItemChanged(assignedPosition);
        }
    }

    public User getSelectedMember() {
        return selectedPosition != -1 ? members.get(selectedPosition) : null;
    }

    public void setItemsEnabled(boolean enabled) {
        this.itemsEnabled = enabled;
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {
        private final AvatarView avatar;
        private final TextView tvName;
        private final ImageView ivCheck;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar_member_task);
            tvName = itemView.findViewById(R.id.tv_name);
            ivCheck = itemView.findViewById(R.id.iv_check);

            itemView.setOnClickListener(v -> {
                if (!itemsEnabled) return;
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    selectMember(position);
                }
            });
        }

        public void bind(User member, boolean isSelected, boolean isAssigned) {
            if (member == null) return;

            String fullName = member.getFullname();
            if (tvName != null) {
                if (fullName != null && !fullName.isEmpty()) {
                    tvName.setVisibility(View.VISIBLE);
                    tvName.setText(fullName);
                } else {
                    tvName.setVisibility(View.GONE);
                }
            }

            if (avatar != null) {
                String avatarUrl = member.getAvatar();
                Log.d("CHECK", avatarUrl);
                if (fullName != null && avatarUrl.equals("img/avatar/default.png")) {
                    avatar.setName(fullName);
                }
                else if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    try {
                        avatar.setImage(android.net.Uri.parse(avatarUrl));
                    } catch (Exception e) {
                        if (fullName != null && !fullName.isEmpty()) {
                            avatar.setName(fullName);
                        }
                    }
                }
            }

            if (ivCheck != null) {
                ivCheck.setVisibility(isSelected || isAssigned ? View.VISIBLE : View.GONE);
            }
        }
    }
} 