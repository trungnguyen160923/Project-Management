package com.example.projectmanagement.ui.notification;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.ui.adapter.NotificationAdapter;
import com.google.android.material.appbar.MaterialToolbar;

public class NotificationActivity extends AppCompatActivity {

    private NotificationViewModel viewModel;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Thiết lập RecyclerView và Adapter
        RecyclerView rvNotifications = findViewById(R.id.rv_notifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        NotificationAdapter adapter = new NotificationAdapter(item -> {
            Toast.makeText(this, "Bạn chọn: " + item.getMessage(), Toast.LENGTH_SHORT).show();
            viewModel.markAsRead(item.getNotificationId());
        });
        rvNotifications.setAdapter(adapter);

        // Lấy ViewModel và observe LiveData
        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        viewModel.filteredNotifications.observe(this, adapter::submitList);

        Spinner spinner = findViewById(R.id.spinner_type);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                switch (pos) {
                    case 0: viewModel.setFilter(NotificationViewModel.Filter.ALL);    break;
                    case 1: viewModel.setFilter(NotificationViewModel.Filter.READ);   break;
                    case 2: viewModel.setFilter(NotificationViewModel.Filter.UNREAD); break;
                }
            }

            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        Button btnMarkAll = findViewById(R.id.btn_mark_all_read);
        btnMarkAll.setOnClickListener(v -> {
            viewModel.markAllRead();
            Toast.makeText(
                    this,
                    "Đã đánh dấu tất cả là đã đọc",
                    Toast.LENGTH_SHORT
            ).show();
        });

        MaterialToolbar toolbar = findViewById(R.id.tb_notifications);
        toolbar.setNavigationOnClickListener(v -> finish());
    }
}