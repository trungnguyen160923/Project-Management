package com.example.projectmanagement.utils;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;

import com.example.projectmanagement.R;

public class LoadingDialog {
    private final Dialog dialog;

    public LoadingDialog(Activity activity) {
        dialog = new Dialog(activity);
        // Loại bỏ tiêu đề
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Gán layout custom loading cho dialog
        dialog.setContentView(R.layout.dialog_loading);
        // Không cho phép người dùng hủy dialog bằng nút Back hoặc nhấn ra ngoài
        dialog.setCancelable(false);
        // Đảm bảo background của dialog trong suốt, để hiển thị đúng layout custom
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    // Hiển thị dialog
    public void show() {
        if (!dialog.isShowing()) {
            if (dialog.getContext() instanceof Activity && !((Activity)dialog.getContext()).isFinishing()) {
                dialog.show();
            }
        }
    }

    // Đóng dialog
    public void dismiss() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }
    public boolean isShowing() {
        return dialog.isShowing();
    }
}
