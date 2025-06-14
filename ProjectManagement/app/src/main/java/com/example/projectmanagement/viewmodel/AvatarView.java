package com.example.projectmanagement.viewmodel;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.graphics.drawable.Drawable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.bumptech.glide.Glide;
import com.example.projectmanagement.R;

/**
 * Custom view hiển thị avatar: ảnh hoặc initials với nền màu.
 */
public class AvatarView extends FrameLayout {
    private ImageView ivAvatar;
    private TextView tvInitials;

    // Danh sách màu nền cho avatar (định nghĩa trong res/values/colors.xml)
    private static final int[] BG_COLORS = {
            R.color.avatar_bg_1,
            R.color.avatar_bg_2,
            R.color.avatar_bg_3,
            R.color.avatar_bg_4,
            R.color.avatar_bg_5,
            R.color.avatar_bg_6
    };

    public AvatarView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_avatar, this);
        ivAvatar    = findViewById(R.id.ivAvatar);
        tvInitials  = findViewById(R.id.tvInitials);
    }

    /**
     * Gán ảnh từ Uri (Glide/Picasso/circle-crop)
     */
    public void setImage(Uri uri) {
        if (uri != null) {
            ivAvatar.setVisibility(VISIBLE);
            tvInitials.setVisibility(GONE);
            Glide.with(getContext())
                    .load(uri)
                    .circleCrop()
                    .into(ivAvatar);
        } else {
            showInitials(null);
        }
    }

    /**
     * Hiển thị initials với nền màu cố định theo tên.
     */
    public void setName(String fullName) {
        showInitials(fullName);
    }

    private void showInitials(String fullName) {
        ivAvatar.setVisibility(GONE);
        tvInitials.setVisibility(VISIBLE);

        // Lấy ký tự initials
        String initials = "?";
        if (!TextUtils.isEmpty(fullName)) {
            String[] parts = fullName.trim().split("\\s+");
            if (parts.length == 1) {
                initials = parts[0].substring(0,1).toUpperCase();
            } else {
                initials = ("" + parts[0].charAt(0) + parts[parts.length-1].charAt(0))
                        .toUpperCase();
            }
        }
        tvInitials.setText(initials);

        // Gán màu nền ngẫu nhiên nhưng cố định theo tên
        int bgColor = getColorForName(fullName);
        // TINT drawable shape để đặt màu nền
        Drawable bg = tvInitials.getBackground().mutate();
        DrawableCompat.setTint(bg, bgColor);
        tvInitials.setBackground(bg);
    }

    /**
     * Tạo màu nền cố định cho mỗi key (fullName)
     */
    private int getColorForName(String key) {
        if (TextUtils.isEmpty(key)) {
            return ContextCompat.getColor(getContext(), BG_COLORS[0]);
        }
        int hash = Math.abs(key.hashCode());
        int index = hash % BG_COLORS.length;
        return ContextCompat.getColor(getContext(), BG_COLORS[index]);
    }
}
