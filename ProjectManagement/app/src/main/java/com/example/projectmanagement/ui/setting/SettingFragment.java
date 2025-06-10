package com.example.projectmanagement.ui.setting;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.SwitchCompat;

import com.example.projectmanagement.databinding.FragmentSettingBinding;
import com.example.projectmanagement.ui.user.ProfileUserActivity;

public class SettingFragment extends Fragment {

    private FragmentSettingBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Lắng nghe sự kiện bật/tắt Switch giao diện
        binding.switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(getContext(), "Đã bật chế độ tối", Toast.LENGTH_SHORT).show();
                // TODO: Thực hiện chuyển sang Dark Mode
            } else {
                Toast.makeText(getContext(), "Đã bật chế độ sáng", Toast.LENGTH_SHORT).show();
                // TODO: Thực hiện chuyển sang Light Mode
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