package com.example.projectmanagement.ui.adapter;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ListAdapter;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.Notification;
import com.example.projectmanagement.utils.ParseDateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NotificationAdapter
        extends ListAdapter<Notification, NotificationAdapter.NotificationViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(@NonNull Notification notification);
    }

    private final OnItemClickListener listener;

    private static final DiffUtil.ItemCallback<Notification> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Notification>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull Notification oldItem,
                        @NonNull Notification newItem) {
                    return Objects.equals(
                            oldItem.getNotificationId(),
                            newItem.getNotificationId()
                    );
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull Notification oldItem,
                        @NonNull Notification newItem) {
                    // So sánh đúng newItem vs oldItem
                    return Objects.equals(oldItem.getMessage(), newItem.getMessage())
                            && Objects.equals(oldItem.getCreatedAt(), newItem.getCreatedAt())
                            && Objects.equals(oldItem.getIsRead(), newItem.getIsRead())
                            && Objects.equals(oldItem.getType(), newItem.getType());
                }
            };



    public NotificationAdapter(@NonNull OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }


    @NonNull @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(root, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {

        CardView cardRoot;
        View viewDot;
        final TextView tvTitle;
        final TextView  tvTime;

        public NotificationViewHolder(@NonNull View itemView, @NonNull OnItemClickListener listener) {
            super(itemView);
            cardRoot = itemView.findViewById(R.id.card_root);
            viewDot  = itemView.findViewById(R.id.view_unread_dot);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvTime  = itemView.findViewById(R.id.tv_time);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(pos));
                }
            });
        }
        void bind(@NonNull Notification item) {
            tvTitle.setText(item.getMessage());
            tvTime.setText(ParseDateUtil.formatDate(item.getCreatedAt()));
            tvTitle.setText(item.getMessage());
            tvTime.setText(ParseDateUtil.formatDate(item.getCreatedAt()));
            if (Boolean.FALSE.equals(item.getIsRead())) {
                // chưa đọc
                cardRoot.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.unread_bg)
                );
                tvTitle.setTypeface(null, Typeface.BOLD);
                tvTitle.setTextColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.text_primary)
                );
                viewDot.setVisibility(View.VISIBLE);
            } else {
                // đã đọc
                cardRoot.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.white)
                );
                tvTitle.setTypeface(null, Typeface.NORMAL);
                tvTitle.setTextColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.text_secondary)
                );
                viewDot.setVisibility(View.GONE);
            }
        }
    }
}