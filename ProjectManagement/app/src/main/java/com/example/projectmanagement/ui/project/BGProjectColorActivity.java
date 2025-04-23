package com.example.projectmanagement.ui.project;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.item.BackgroundColorItem;
import com.example.projectmanagement.databinding.ActivityBgprojectColorBinding;
import com.example.projectmanagement.ui.adapter.BackgroundColorAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.example.projectmanagement.ui.project.BackgroundConstants.*;

public class BGProjectColorActivity extends AppCompatActivity {
    private ActivityBgprojectColorBinding binding;
    private List<BackgroundColorItem> backgroundList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBgprojectColorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initData();
        binding.gridViewBGColor.setAdapter(
                new BackgroundColorAdapter(this, R.layout.cell_color_bg, new ArrayList<>(backgroundList))
        );

        binding.gridViewBGColor.setOnItemClickListener((parent, view, pos, id) -> {
            BackgroundColorItem item = backgroundList.get(pos);
            Intent data = new Intent()
                    .putExtra(EXTRA_TYPE, TYPE_COLOR)
                    .putExtra(EXTRA_START_COLOR_RES_ID, item.getStartColorResId());

            Integer endRes = item.getEndColorResId();
            if (endRes != null) {
                data.putExtra(EXTRA_END_COLOR_RES_ID, endRes)
                        .putExtra(EXTRA_ORIENTATION_ORDINAL, item.getGradientOrientation().ordinal());
            }

            setResult(RESULT_OK, data);
            finish();
        });

        binding.btnBackgroundProjectColorBack.setOnClickListener(v -> finish());
    }

    private void initData() {
        backgroundList = new ArrayList<>();
        // Gradient items
        int[][] gradients = {
                {R.color.g11_start, R.color.g11_end},
                {R.color.g12_start, R.color.g12_end},
                {R.color.g13_start, R.color.g13_end},
                {R.color.g21_start, R.color.g21_end},
                {R.color.g22_start, R.color.g22_end},
                {R.color.g23_start, R.color.g23_end},
                {R.color.g31_start, R.color.g31_end},
                {R.color.g32_start, R.color.g32_end},
                {R.color.g33_start, R.color.g33_end}
        };
        for (int[] g : gradients) {
            backgroundList.add(
                    new BackgroundColorItem(
                            g[0], g[1], R.drawable.snowflake,
                            GradientDrawable.Orientation.TOP_BOTTOM
                    )
            );
        }
        // Solid colors
        int[] solids = {
                R.color.s41, R.color.s42, R.color.s43,
                R.color.s51, R.color.s52, R.color.s53,
                R.color.s61, R.color.s62, R.color.s63
        };
        for (int color : solids) {
            backgroundList.add(new BackgroundColorItem(color));
        }
    }
}
