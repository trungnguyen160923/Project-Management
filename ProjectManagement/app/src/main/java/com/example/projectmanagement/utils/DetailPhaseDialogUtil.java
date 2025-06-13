package com.example.projectmanagement.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.data.model.ProjectHolder;
import com.example.projectmanagement.data.service.PhaseService;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DetailPhaseDialogUtil {
    private static final String TAG = "DetailPhaseDialogUtil";

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

                // Validate input
                if (newName.isEmpty()) {
                    Toast.makeText(context, "Tên giai đoạn không được để trống", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Hiển thị loading
                LoadingDialog loadingDialog = new LoadingDialog((Activity) context);
                loadingDialog.show();

                // Gọi API cập nhật phase
                PhaseService.updatePhase(
                        context,
                        phase.getPhaseID(),
                        ProjectHolder.get().getProjectID(),
                        newName,
                        newDesc,
                        response -> {
                            loadingDialog.dismiss();
                            try {
                                String status = response.optString("status", "error");
                                String msg = response.optString("error", null);

                                if ("success".equals(status)) {
                                    // Cập nhật thành công
                                    phase.setPhaseName(newName);
                                    phase.setDescription(newDesc);
                                    listener.onPhaseUpdated(phase);
                                    Toast.makeText(context, "Cập nhật giai đoạn thành công!", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                } else {
                                    // Cập nhật thất bại
                                    String errorMsg = !msg.isEmpty() && !msg.equals("null") ? msg : "Không thể cập nhật giai đoạn";
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing response", e);
                                Toast.makeText(context, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                            }
                        },
                        error -> {
                            loadingDialog.dismiss();
                            String errorMessage = "Lỗi không xác định";
                            try {
                                errorMessage = Helpers.parseError(error);
                            } catch (Exception e) {}
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Update phase error", error);
                        }
                );
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
                    // Hiển thị loading
                    LoadingDialog loadingDialog = new LoadingDialog((Activity) ctx);
                    loadingDialog.show();

                    // Gọi API xóa phase
                    PhaseService.deletePhase(
                            ctx,
                            phase.getPhaseID(),
                            response -> {
                                loadingDialog.dismiss();
                                try {
                                    String status = response.optString("status", "error");
                                    String msg = response.optString("error", null);

                                    if ("success".equals(status)) {
                                        // Xóa thành công
                                        listener.onPhaseDeleted(phase);
                                        Toast.makeText(ctx, "Xóa giai đoạn thành công!", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    } else {
                                        // Xóa thất bại
                                        String errorMsg = !msg.isEmpty() && !msg.equals("null") ? msg : "Không thể xóa giai đoạn";
                                        Toast.makeText(ctx, errorMsg, Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing response", e);
                                    Toast.makeText(ctx, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                                }
                            },
                            error -> {
                                loadingDialog.dismiss();
                                String errorMessage = "Lỗi không xác định";
                                try {
                                    errorMessage = Helpers.parseError(error);
                                } catch (Exception e) {}
                                Toast.makeText(ctx, errorMessage, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Delete phase error", error);
                            }
                    );
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }
}
