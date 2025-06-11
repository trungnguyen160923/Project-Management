package com.example.projectmanagement.ui.adapter;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.File;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ImageAttachmentAdapter
        extends RecyclerView.Adapter<ImageAttachmentAdapter.ViewHolder> {

    public interface OnAttachmentActionListener {
        void onDownloadClicked(int position);
        void onDeleteClicked(int position);
    }

    private List<Uri> images;
    private final OnAttachmentActionListener listener;
    private final Context context;
    private final SimpleDateFormat dateFormat;

    public ImageAttachmentAdapter(Context context, List<Uri> images, OnAttachmentActionListener listener) {
        this.context = context;
        this.images = images;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    public void updateUris(List<Uri> newUris) {
        this.images = newUris;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_attachment, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Uri uri = images.get(pos);
        h.imgPreview.setImageURI(uri);

        // Xử lý click vào ảnh
        h.imgPreview.setOnClickListener(v -> showImageDetailDialog(pos));

        h.btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.inflate(R.menu.menu_attachment_item);

            // Force icons to show on Android P+
            try {
                Field mField = popup.getClass().getDeclaredField("mPopup");
                mField.setAccessible(true);
                Object menuHelper = mField.get(popup);
                Method setForceIcons = menuHelper.getClass()
                        .getDeclaredMethod("setForceShowIcon", boolean.class);
                setForceIcons.invoke(menuHelper, true);
            } catch (Exception ignored) {}

            popup.setOnMenuItemClickListener(item -> {
                if(item.getItemId() == R.id.action_download){
                    listener.onDownloadClicked(pos);
                    return true;
                }else if(item.getItemId() == R.id.action_delete){
                    listener.onDeleteClicked(pos);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private void showImageDetailDialog(int position) {
        Uri uri = images.get(position);
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_image_detail);

        // Ánh xạ views
        ImageView imgPreview = dialog.findViewById(R.id.imgPreview);
        TextView tvFileName = dialog.findViewById(R.id.tvFileName);
        TextView tvFileSize = dialog.findViewById(R.id.tvFileSize);
        TextView tvFileType = dialog.findViewById(R.id.tvFileType);
        TextView tvCreatedAt = dialog.findViewById(R.id.tvCreatedAt);
        Button btnDownload = dialog.findViewById(R.id.btnDownload);
        Button btnDelete = dialog.findViewById(R.id.btnDelete);
        ImageButton btnClose = dialog.findViewById(R.id.btnClose);

        // Set data
        imgPreview.setImageURI(uri);
        tvFileName.setText(getFileName(uri));
        tvFileSize.setText(formatFileSize(getFileSize(uri)));
        tvFileType.setText("Image");
        tvCreatedAt.setText(dateFormat.format(new java.util.Date()));

        // Set click listeners
        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnDownload.setOnClickListener(v -> {
            listener.onDownloadClicked(position);
            dialog.dismiss();
        });
        btnDelete.setOnClickListener(v -> {
            listener.onDeleteClicked(position);
            dialog.dismiss();
        });

        dialog.show();
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf(java.io.File.separator);
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private long getFileSize(Uri uri) {
        try {
            android.database.Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    return cursor.getLong(sizeIndex);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format(Locale.getDefault(), "%.1f %s", 
            size / Math.pow(1024, digitGroups), 
            units[digitGroups]);
    }

    @Override
    public int getItemCount() {
        return images != null ? images.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPreview;
        ImageButton btnMore;

        ViewHolder(View itemView) {
            super(itemView);
            imgPreview = itemView.findViewById(R.id.img_preview);
            btnMore    = itemView.findViewById(R.id.btn_image_more);
        }
    }
}
