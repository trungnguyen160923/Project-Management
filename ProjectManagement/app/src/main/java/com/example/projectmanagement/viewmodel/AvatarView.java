package com.example.projectmanagement.viewmodel;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.projectmanagement.R;

public class AvatarView extends FrameLayout {
    private ImageView ivAvatar;
    private TextView tvInitials;

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
            // Ví dụ với Glide:
            Glide.with(getContext())
                    .load(uri)
                    .circleCrop()
                    .into(ivAvatar);
        } else {
            showInitials(null);
        }
    }

    /**
     * Hiển thị initials; nếu name null hoặc rỗng thì để mặc định icon?
     */
    public void setName(String fullName) {
        showInitials(fullName);
    }

    private void showInitials(String fullName) {
        ivAvatar.setVisibility(GONE);
        tvInitials.setVisibility(VISIBLE);

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
    }
}

