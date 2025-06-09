package com.example.projectmanagement.ui.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class ImageAttachmentAdapter
        extends RecyclerView.Adapter<ImageAttachmentAdapter.ViewHolder> {

    public interface OnAttachmentActionListener {
        void onDownloadClicked(int position);
        void onDeleteClicked(int position);
    }

    private final List<Uri> images;
    private final OnAttachmentActionListener listener;

    public ImageAttachmentAdapter(List<Uri> images, OnAttachmentActionListener listener) {
        this.images = images;
        this.listener = listener;
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

    @Override
    public int getItemCount() {
        return images.size();
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
