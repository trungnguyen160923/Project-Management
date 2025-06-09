package com.example.projectmanagement.ui.adapter;

import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.utils.ParseDateUtil;
import java.util.ArrayList;
import java.util.List;
import android.graphics.Color;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {
    private final List<Project> projects = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Project project);
    }

    public ProjectAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<Project> data) {
        projects.clear();
        if (data != null) projects.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType
    ) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false));
    }

    @Override public void onBindViewHolder(
            @NonNull ViewHolder holder, int pos
    ) {
        Project p = projects.get(pos);
        holder.bind(p);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(p);
        });
    }

    @Override public int getItemCount() {
        return projects.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBg;
        TextView tvName, tvDesc, tvDeadline;

        ViewHolder(@NonNull View v) {
            super(v);
            ivBg      = v.findViewById(R.id.ivItemBackground);
            tvName    = v.findViewById(R.id.tvProjectName);
            tvDesc    = v.findViewById(R.id.tvDescription);
            tvDeadline= v.findViewById(R.id.tvDeadline);
        }

        void bind(Project p) {
            tvName.setText(p.getProjectName());
            // description
            if (TextUtils.isEmpty(p.getProjectDescription())) {
                tvDesc.setVisibility(View.GONE);
            } else {
                tvDesc.setVisibility(View.VISIBLE);
                tvDesc.setText(p.getProjectDescription());
            }
            // deadline
            if (p.getDeadline()!=null) {
                tvDeadline.setVisibility(View.VISIBLE);
                tvDeadline.setText("Hạn: "+ParseDateUtil.formatDate(p.getDeadline()));
            } else tvDeadline.setVisibility(View.GONE);

            String bg = p.getBackgroundImg();
            if (bg.startsWith("http")) {
                Glide.with(ivBg.getContext()).load(bg).into(ivBg);
            } else if (bg.startsWith("COLOR;")) {
                ivBg.setBackgroundColor(Color.parseColor(bg.split(";",2)[1]));
            } else if (bg.startsWith("GRADIENT;")) {
                String[] parts = bg.split(";",3);
                String[] cols  = parts[1].split(",");
                int c1 = Color.parseColor(cols[0]);
                int c2 = Color.parseColor(cols[1]);
                int ori = Integer.parseInt(parts[2]);
                GradientDrawable gd = new GradientDrawable(
                        GradientDrawable.Orientation.values()[ori],
                        new int[]{c1,c2});
                ivBg.setBackground(gd);
            } else if (bg.startsWith("RESOURCE;")) {
                ivBg.setImageResource(Integer.parseInt(bg.split(";",2)[1]));
            }
        }
    }
}
