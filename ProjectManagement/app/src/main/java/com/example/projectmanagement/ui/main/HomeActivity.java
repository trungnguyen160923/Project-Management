package com.example.projectmanagement.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.User;
import com.example.projectmanagement.databinding.ActivityHomeBinding;
import com.example.projectmanagement.ui.auth.LoginActivity;
import com.example.projectmanagement.ui.auth.vm.LoginViewModel;
import com.example.projectmanagement.ui.notification.NotificationActivity;
import com.example.projectmanagement.ui.project.CreateProjectActivity;
import com.example.projectmanagement.viewmodel.AvatarView;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHomeBinding binding;
    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo ViewModel
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        setSupportActionBar(binding.appBarHome.toolbar);
        binding.appBarHome.btnAddPhase.setOnClickListener(view -> 
            startActivity(new Intent(HomeActivity.this, CreateProjectActivity.class))
        );

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Cập nhật thông tin người dùng trong navigation header
        View headerView = navigationView.getHeaderView(0);
        AvatarView avatarView = headerView.findViewById(R.id.avatar_view_nav);
        TextView tvName = headerView.findViewById(R.id.main_tv_nav);
        TextView tvEmail = headerView.findViewById(R.id.tv_email_nav);

        // Lấy thông tin user từ ViewModel
        User currentUser = loginViewModel.getCurrentUser();
        if (currentUser != null) {
            tvName.setText(currentUser.getFullname());
            tvEmail.setText(currentUser.getEmail());
            
            // Xử lý avatar
            String avatarPath = currentUser.getAvatar();
            Log.d("AvatarDebug", "Avatar path: [" + avatarPath + "]");

            if ("img/avatar/default.png".equals(avatarPath.replace("\\/", "/"))) {
                avatarView.setName(currentUser.getFullname());
                Log.d("AvatarDebug", "Using name as avatar (normalized match)");
            }
            else if (!avatarPath.isEmpty()) {
                // Nếu là URL đầy đủ
                if (avatarPath.startsWith("http")) {
                    avatarView.setImage(Uri.parse(avatarPath));
                    Log.d("AvatarDebug", "Using full URL");
                } 
                // Nếu là đường dẫn tương đối, thêm base URL
                else {
                    avatarView.setImage(Uri.parse(currentUser.getAvatar()));
                    Log.d("AvatarDebug", "Using relative path with base URL");
                }
            }
        }

        // Xử lý nút logout
        headerView.findViewById(R.id.button_logout).setOnClickListener(v -> {
            loginViewModel.logout();
            // Chuyển về màn hình đăng nhập
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_phase, R.id.nav_myTasks, R.id.nav_statistics, R.id.nav_setting)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.search) {
            Toast.makeText(this, "Bạn chọn Search", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if (id == R.id.notification) {
            // Khởi động NotificationActivity
            Intent intent = new Intent(this, NotificationActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}