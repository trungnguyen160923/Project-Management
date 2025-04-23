package com.example.projectmanagement.ui.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.item.BackgroundColorItem;

import java.util.List;

public class BackgroundColorAdapter extends BaseAdapter {
    private Context context;
    private int layoutResId;
    private List<BackgroundColorItem> backgroundList;
    private LayoutInflater inflater;
    private final float cornerRadiusPx;

    public BackgroundColorAdapter(Context context,
                                  int layoutResId,
                                  List<BackgroundColorItem> backgroundList) {
        this.context = context;
        this.layoutResId = layoutResId;
        this.backgroundList = backgroundList;
        this.inflater = LayoutInflater.from(context);
        this.cornerRadiusPx = 16 * context.getResources().getDisplayMetrics().density;
    }

    @Override
    public int getCount() {
        return backgroundList.size();
    }

    @Override
    public Object getItem(int position) {
        return backgroundList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(layoutResId, parent, false);
            holder = new ViewHolder();
            holder.viewBackground = convertView.findViewById(R.id.viewBackground);
            holder.imgIcon        = convertView.findViewById(R.id.imgIcon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        BackgroundColorItem item = backgroundList.get(position);

        // Tạo GradientDrawable động
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(cornerRadiusPx);

        if (item.getEndColorResId() != null) {
            int startColor = ContextCompat.getColor(context, item.getStartColorResId());
            int endColor   = ContextCompat.getColor(context, item.getEndColorResId());
            gd.setColors(new int[]{ startColor, endColor });
            // Sử dụng orientation tùy chỉnh nếu có
            if (item.getGradientOrientation() != null) {
                gd.setOrientation(item.getGradientOrientation());
            } else {
                gd.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
            }
        } else {
            int color = ContextCompat.getColor(context, item.getStartColorResId());
            gd.setColor(color);
        }
        holder.viewBackground.setBackground(gd);

        // Xử lý icon (nếu có)
        if (item.getIconResId() != null) {
            holder.imgIcon.setVisibility(View.VISIBLE);
            holder.imgIcon.setImageResource(item.getIconResId());
        } else {
            holder.imgIcon.setVisibility(View.GONE);
        }

        return convertView;
    }

    private static class ViewHolder {
        View viewBackground;
        ImageView imgIcon;
    }
}
