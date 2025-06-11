package com.example.projectmanagement.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.Phase;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DetailPhaseDialogUtil {

    /** Callback để nhận sự kiện khi user cập nhật hoặc xóa Phase */
    public interface PhaseDialogListener {
        /** Gọi khi user lưu thay đổi */
        void onPhaseUpdated(Phase updatedPhase);
        /** Gọi khi user xác nhận xóa */
        void onPhaseDeleted(Phase phaseToDelete);
    }

    /**
     * Hiển thị dialog chi tiết Phase
     *
     * @param context   Context (Activity/Fragment)
     * @param phase     Phase cần show
     * @param listener  Callback xử lý update/delete
     */
    public static void showPhaseDetailDialog(
            Context context,
            Phase phase,
            PhaseDialogListener listener
    ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_phase_detail, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        ImageButton btnClose    = view.findViewById(R.id.btn_close);
        TextInputEditText etName= view.findViewById(R.id.et_phase_name);
        TextInputEditText etDesc= view.findViewById(R.id.et_description);
        Button btnUpdate        = view.findViewById(R.id.btn_update);
        Button btnDelete        = view.findViewById(R.id.btn_delete);

        // Đổ dữ liệu ban đầu
        etName.setText(phase.getPhaseName());
        etDesc.setText(phase.getDescription());

        // lưu giá trị gốc để có thể phục hồi khi Cancel
        final String origName = phase.getPhaseName();
        final String origDesc = phase.getDescription();

        final boolean[] isEditing = {false};

        // Hàm khôi phục UI về chế độ xem (view-only)
        Runnable exitEditMode = () -> {
            isEditing[0] = false;
            etName.setEnabled(false);
            etDesc.setEnabled(false);
            btnUpdate.setText("Cập nhật");
            btnDelete.setText("Xoá");
            // phục hồi dữ liệu gốc
            etName.setText(origName);
            etDesc.setText(origDesc);
            // reset lại listener của Delete thành xoá phase
            btnDelete.setOnClickListener(v -> confirmDelete(context, phase, listener, dialog));
        };

        // listener confirm xoá phase
        btnDelete.setOnClickListener(v -> confirmDelete(context, phase, listener, dialog));

        // Xử lý nút Update / Save
        btnUpdate.setOnClickListener(v -> {
            if (!isEditing[0]) {
                // vào chế độ edit
                isEditing[0] = true;
                etName.setEnabled(true);
                etDesc.setEnabled(true);
                btnUpdate.setText("Lưu");
                btnDelete.setText("Huỷ");
                // gán lại listener cho nút Huỷ
                btnDelete.setOnClickListener(x -> {
                    new AlertDialog.Builder(context)
                            .setTitle("Hủy thay đổi")
                            .setMessage("Bạn có chắc muốn hủy bỏ thay đổi?")
                            .setPositiveButton("Có", (d,w) -> {
                                exitEditMode.run();
                            })
                            .setNegativeButton("Không", null)
                            .show();
                });
            } else {
                // lưu thay đổi
                String newName = etName.getText().toString().trim();
                String newDesc = etDesc.getText().toString().trim();
                phase.setPhaseName(newName);
                phase.setDescription(newDesc);
                listener.onPhaseUpdated(phase);
                dialog.dismiss();
            }
        });

        // Nút đóng góc phải
        btnClose.setOnClickListener(v -> {
            if (isEditing[0]) {
                new AlertDialog.Builder(context)
                        .setTitle("Xác nhận")
                        .setMessage("Thoát mà không lưu thay đổi?")
                        .setPositiveButton("Có", (d, w) -> dialog.dismiss())
                        .setNegativeButton("Không", null)
                        .show();
            } else {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /** Hiển thị confirm xóa phase và gọi callback */
    private static void confirmDelete(
            Context ctx,
            Phase phase,
            DetailPhaseDialogUtil.PhaseDialogListener listener,
            AlertDialog dialog
    ) {
        new AlertDialog.Builder(ctx)
                .setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc muốn xoá giai đoạn này?")
                .setPositiveButton("Xoá", (d, w) -> {
                    listener.onPhaseDeleted(phase);
                    dialog.dismiss();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

}
