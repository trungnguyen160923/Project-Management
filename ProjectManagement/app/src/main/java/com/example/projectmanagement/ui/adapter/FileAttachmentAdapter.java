package com.example.projectmanagement.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.List;

public class FileAttachmentAdapter
        extends RecyclerView.Adapter<FileAttachmentAdapter.ViewHolder> {

    public interface OnAttachmentActionListener {
        void onDownloadClicked(int position);
        void onDeleteClicked(int position);
    }

    private List<File> files;
    private final OnAttachmentActionListener listener;

    public FileAttachmentAdapter(List<File> files, OnAttachmentActionListener listener) {
        this.files = files;
        this.listener = listener;
    }

    public void updateFiles(List<File> newFiles) {
        this.files = newFiles;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file_attachment, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        File f = files.get(pos);
        h.imgIcon.setImageResource(getIconForExtension(f.getFileType()));
        h.tvName.setText(f.getFileName());
        h.tvSize.setText(String.valueOf(f.getFileSize()));

        h.btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.inflate(R.menu.menu_attachment_item);

            // Ép hiển thị icon trong menu (Android P+)
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

    @Override public int getItemCount() {
        return files != null ? files.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView tvName, tvSize;
        ImageButton btnMore;
        ViewHolder(View item) {
            super(item);
            imgIcon = item.findViewById(R.id.img_file_icon);
            tvName  = item.findViewById(R.id.tv_file_name);
            tvSize  = item.findViewById(R.id.tv_file_size);
            btnMore = item.findViewById(R.id.btn_file_more);
        }
    }

    private int getIconForExtension(String ext) {
        return R.drawable.ic_file;
    }
}
