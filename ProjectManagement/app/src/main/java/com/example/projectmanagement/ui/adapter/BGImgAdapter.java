package com.example.projectmanagement.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.item.BGImgItem;
import com.example.projectmanagement.data.item.BackgroundColorItem;

import java.util.List;

public class BGImgAdapter extends BaseAdapter {
    private Context context;
    private int layoutResId;
    private List<BGImgItem> backgroundList;
    private LayoutInflater inflater;

    public BGImgAdapter(Context ctx, int layoutResId, List<BGImgItem> list) {
        this.context     = ctx;
        this.layoutResId  = layoutResId;
        this.backgroundList = list;
        this.inflater     = LayoutInflater.from(ctx);
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
            holder.ivImage = convertView.findViewById(R.id.iv_bg_img);
            holder.tvName  = convertView.findViewById(R.id.tv_img_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // bind data
        BGImgItem item = backgroundList.get(position);
        holder.ivImage.setImageResource(item.getImg());
        holder.tvName .setText(item.getName());

        return convertView;
    }

    private static class ViewHolder {
        ImageView ivImage;
        TextView  tvName;
    }
}

