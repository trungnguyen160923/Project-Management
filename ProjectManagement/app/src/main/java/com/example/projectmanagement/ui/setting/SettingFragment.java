package com.example.projectmanagement.ui.setting;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.projectmanagement.databinding.FragmentSettingBinding;
import com.example.projectmanagement.ui.user.ProfileUserActivity;

public class SettingFragment extends Fragment {
    private FragmentSettingBinding binding;
    private SettingViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(SettingViewModel.class);
        viewModel.init(requireContext());

        // Observe dark mode state
        viewModel.getDarkMode().observe(getViewLifecycleOwner(), isDark -> {
            binding.switchTheme.setChecked(isDark);
        });

        // Lắng nghe sự kiện bật/tắt Switch giao diện
        binding.switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setDarkMode(isChecked);
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                Toast.makeText(getContext(), "Đã bật chế độ tối", Toast.LENGTH_SHORT).show();
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Toast.makeText(getContext(), "Đã bật chế độ sáng", Toast.LENGTH_SHORT).show();
            }
        });

        // Sự kiện click Hồ sơ
        binding.layoutProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileUserActivity.class);
            startActivity(intent);
        });

        // Sự kiện click Mở thiết lập hệ thống (Notification)
        binding.layoutNotificationSetting.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
            startActivity(intent);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}