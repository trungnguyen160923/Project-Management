package com.example.projectmanagement.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.utils.ParseDateUtil;

import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {
    private final Context context;
    private List<Project> projectList;

    public ProjectAdapter(Context context, List<Project> projectList) {
        this.context = context;
        this.projectList = projectList;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<Project> projects) {
        this.projectList = projects;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_project, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Project p = projectList.get(position);
        holder.tvName.setText(p.getProjectName());

        // Mô tả
        if (TextUtils.isEmpty(p.getProjectDescription())) {
            holder.tvDesc.setVisibility(View.GONE);
        } else {
            holder.tvDesc.setText(p.getProjectDescription());
            holder.tvDesc.setVisibility(View.VISIBLE);
        }

        // Deadline
        if (p.getDeadline() != null) {
            holder.tvDeadline.setText("Hạn: " + ParseDateUtil.formatDate(p.getDeadline()));
            holder.tvDeadline.setVisibility(View.VISIBLE);
        } else {
            holder.tvDeadline.setVisibility(View.GONE);
        }

        // Background: có thể là URL, màu, hoặc gradient
        String bg = p.getBackgroundImg();
        if (bg.startsWith("http")) {
            Glide.with(context).load(bg).into(holder.ivBg);
        } else if (bg.startsWith("COLOR;")) {
            holder.ivBg.setBackgroundColor(Color.parseColor(bg.split(";")[1]));
        } else if (bg.startsWith("GRADIENT;")) {
            // Format GRADIENT;#start,#end;ORIENTATION
            String[] parts = bg.split(";", 3);
            String[] hex = parts[1].split(",");
            int colorStart = Color.parseColor(hex[0]);
            int colorEnd   = Color.parseColor(hex[1]);

            // phần [2] = "2" -> ordinal
            int oriIndex = Integer.parseInt(parts[2]);
            // lấy đúng enum bằng ordinal
            GradientDrawable.Orientation ori =
                    GradientDrawable.Orientation.values()[oriIndex];

            GradientDrawable shape = new GradientDrawable(ori, new int[]{colorStart, colorEnd});
            holder.ivBg.setBackground(shape);
        }else if(bg.startsWith("RESOURCE;")){
            holder.ivBg.setImageResource(Integer.parseInt(bg.split(";")[1]));
        }
    }

    @Override public int getItemCount() {
        return projectList == null ? 0 : projectList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBg;
        TextView tvName, tvDesc, tvDeadline;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBg       = itemView.findViewById(R.id.ivItemBackground);
            tvName     = itemView.findViewById(R.id.tvProjectName);
            tvDesc     = itemView.findViewById(R.id.tvDescription);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
        }
    }
}

