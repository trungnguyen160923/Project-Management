package com.example.projectmanagement.ui.project;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projectmanagement.databinding.ActivityBackGroundProjectBinding;

import static com.example.projectmanagement.ui.project.BackgroundConstants.*;

public class BackGroundProjectActivity extends AppCompatActivity {
    private ActivityBackGroundProjectBinding binding;
    private ActivityResultLauncher<Intent> bgLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBackGroundProjectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bgLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        setResult(RESULT_OK, result.getData());
                        finish();
                    }
                }
        );

        binding.btnBackgroundProjectBack.setOnClickListener(v -> finish());
        binding.btnBackgroundProjectColor.setOnClickListener(v ->
                bgLauncher.launch(new Intent(this, BGProjectColorActivity.class))
        );
        binding.btnBackgroundProjectImg.setOnClickListener(v ->
                bgLauncher.launch(new Intent(this, BGProjectImgActivity.class))
        );
    }
}