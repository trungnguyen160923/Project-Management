package com.example.projectmanagement.ui.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.File;
import com.example.projectmanagement.utils.ParseDateUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileAttachmentAdapter extends RecyclerView.Adapter<FileAttachmentAdapter.ViewHolder> {
    private final List<File> files;
    private final OnAttachmentActionListener listener;
    private final Context context;
    private final SimpleDateFormat dateFormat;

    public FileAttachmentAdapter(Context context, List<File> files, OnAttachmentActionListener listener) {
        this.context = context;
        this.files = files;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("d MMM yyyy 'lúc' HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file_attachment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File file = files.get(position);
        holder.tvFileName.setText(file.getFileName());
        // Ví dụ: "121 B, 6 ngày trước"
        String info = formatFileSize(file.getFileSize()) + ", " + ParseDateUtil.formatDate((file.getCreatedAt()));
        holder.tvFileInfo.setText(info);

        // Set icon theo loại file
        holder.ivFileIcon.setImageResource(getFileIconResource(file.getFileType()));

        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.btnMore);
            popup.inflate(R.menu.menu_attachment_item);
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_download) {
                    listener.onDownloadClicked(position);
                    return true;
                } else if (item.getItemId() == R.id.action_delete) {
                    listener.onDeleteClicked(position);
                    return true;
                }
                return false;
            });
            popup.show();
        });

        // Click vào toàn bộ item để hiển thị dialog chi tiết
        holder.itemView.setOnClickListener(v -> showFileDetailDialog(position));
    }


    private static final int CREATE_FILE_REQUEST_CODE = 1001;
    private File currFileToSave;

    private void showFileDetailDialog(int position) {
        File file = files.get(position);
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_file_detail);

        // Set dialog width to match parent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        ImageView ivFileIcon = dialog.findViewById(R.id.ivFileIcon);
        TextView tvFileName = dialog.findViewById(R.id.tvFileName);
        TextView tvFileSize = dialog.findViewById(R.id.tvFileSize);
        TextView tvFileType = dialog.findViewById(R.id.tvFileType);
        TextView tvFileDate = dialog.findViewById(R.id.tvFileDate);
        Button btnDownload = dialog.findViewById(R.id.btnDownload);
        Button btnDelete = dialog.findViewById(R.id.btnDelete);

        // Set file icon
        int iconResId = getFileIconResource(file.getFileType());
        ivFileIcon.setImageResource(iconResId);

        // Set file details
        tvFileName.setText(file.getFileName());
        tvFileSize.setText("Kích thước: " + formatFileSize(file.getFileSize()));
        tvFileType.setText("Loại file: " + file.getFileType().toUpperCase());
        tvFileDate.setText("Ngày tạo: " + dateFormat.format(new Date()));

        // Set click listeners
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

    private int getFileIconResource(String fileType) {
        switch (fileType.toLowerCase()) {
            case "pdf":
                return R.drawable.ic_pdf;
            case "doc":
            case "docx":
                return R.drawable.ic_word;
            case "xls":
            case "xlsx":
                return R.drawable.ic_excel;
            case "ppt":
            case "pptx":
                return R.drawable.ic_powerpoint;
            case "txt":
                return R.drawable.ic_text;
            case "zip":
            case "rar":
                return R.drawable.ic_zip;
            default:
                return R.drawable.ic_file;
        }
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
        return files.size();
    }

    public void updateFiles(List<File> newFiles) {
        files.clear();
        if (newFiles != null) {
            files.addAll(newFiles);
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFileIcon, btnMore;
        TextView tvFileName, tvFileInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFileIcon = itemView.findViewById(R.id.ivFileIcon);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvFileInfo = itemView.findViewById(R.id.tvFileInfo);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }

    public interface OnAttachmentActionListener {
        void onDownloadClicked(int position);
        void onDeleteClicked(int position);
    }
}
