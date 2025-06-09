package com.example.projectmanagement.ui.project;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.item.BGImgItem;
import com.example.projectmanagement.databinding.ActivityBgprojectImgBinding;
import com.example.projectmanagement.ui.adapter.BGImgAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.example.projectmanagement.ui.project.BackgroundConstants.*;

public class BGProjectImgActivity extends AppCompatActivity {
    private ActivityBgprojectImgBinding binding;
    private List<BGImgItem> bgImgItems;
    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBgprojectImgBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initData();
        binding.gridViewBGImg.setAdapter(
                new BGImgAdapter(this, R.layout.cell_img_bg, new ArrayList<>(bgImgItems))
        );
        binding.gridViewBGImg.setOnItemClickListener((parent, view, pos, id) -> {
            BGImgItem item = bgImgItems.get(pos);
            Intent data = new Intent()
                    .putExtra(EXTRA_TYPE, TYPE_IMG)
                    .putExtra(EXTRA_IMG_RES, item.getImg());
            setResult(RESULT_OK, data);
            finish();
        });
        binding.btnBackgroundProjectImgBack.setOnClickListener(v -> finish());

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(), uri -> {
                    if (uri != null) {
                        Intent data = new Intent()
                                .putExtra(EXTRA_TYPE, TYPE_IMG)
                                .putExtra(EXTRA_IMG_URI, uri);
                        setResult(RESULT_OK, data);
                        finish();
                    }
                }
        );
        binding.btnChooseImg.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
    }

    private void initData() {
        bgImgItems = new ArrayList<>();
        bgImgItems.add(new BGImgItem(R.drawable.bg1, "Wietse Jongsma"));
        bgImgItems.add(new BGImgItem(R.drawable.bg2, "Marek Piwnicki"));
        bgImgItems.add(new BGImgItem(R.drawable.bg3, "Ingmar H"));
        bgImgItems.add(new BGImgItem(R.drawable.bg4, "Philipp"));
        bgImgItems.add(new BGImgItem(R.drawable.bg5, "Dextar Vision"));
        bgImgItems.add(new BGImgItem(R.drawable.bg6, "Bug Vision"));
        bgImgItems.add(new BGImgItem(R.drawable.bg7, "Leo Messi"));
    }
}
