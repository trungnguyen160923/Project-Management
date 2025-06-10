package com.example.projectmanagement.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.projectmanagement.R;
import com.google.android.material.button.MaterialButton;

public class ConfirmDialogUtil {
    public interface ConfirmCallback {
        void onConfirm();
    }

    public static void show(Context context, String title, String message, String textConfirm, String textCancel, int iconResId, ConfirmCallback callback) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_confirm, null);

        // Set dynamic content
        ((TextView) dialogView.findViewById(R.id.tv_title)).setText(title);
        ((TextView) dialogView.findViewById(R.id.tv_message)).setText(message);
        if (iconResId != 0) {
            ((ImageView) dialogView.findViewById(R.id.iv_icon)).setImageResource(iconResId);
        }
        MaterialButton btnConfirm = dialogView.findViewById(R.id.btn_confirm);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        btnConfirm.setText(textConfirm);
        btnCancel.setText(textCancel);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            if (callback != null) callback.onConfirm();
        });

        dialog.show();
    }
}
