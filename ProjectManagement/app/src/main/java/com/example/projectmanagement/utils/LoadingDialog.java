package com.example.projectmanagement.utils;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

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
            // Đặt dialog ở giữa màn hình
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.gravity = android.view.Gravity.CENTER;
            dialog.getWindow().setAttributes(params);
        }
    }

    // Hiển thị dialog
    public void show() {
        if (!dialog.isShowing()) {
            if (dialog.getContext() instanceof Activity && !((Activity)dialog.getContext()).isFinishing()) {
                Log.d("LoadingDialog", "Showing loading dialog");
                dialog.show();
            } else {
                Log.e("LoadingDialog", "Cannot show dialog: Context is not Activity or Activity is finishing");
            }
        } else {
            Log.d("LoadingDialog", "Dialog is already showing");
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
