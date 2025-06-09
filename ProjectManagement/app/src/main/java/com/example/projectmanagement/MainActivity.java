package com.example.projectmanagement;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.projectmanagement.ui.auth.LoginActivity;
import com.example.projectmanagement.ui.auth.RegisterActivity;
import com.example.projectmanagement.ui.main.HomeActivity;
import com.example.projectmanagement.ui.project.ProjectActivity;
import com.example.projectmanagement.ui.task.TaskActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    // Sử dụng AtomicBoolean đảm bảo an toàn cho các luồng xử lý dữ liệu
    private final AtomicBoolean isDataReady = new AtomicBoolean(false);

    Button btn_signIn, btn_signUp;

    private static final String ACTION_SIGN_UP = "SIGN_UP";
    private static final String ACTION_SIGN_IN = "SIGN_IN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Cài đặt Splash Screen ngay trước super.onCreate()
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_signIn = findViewById(R.id.sign_in_btn);
        btn_signUp = findViewById(R.id.sign_up_btn);



        // Giữ splash screen hiển thị cho đến khi dữ liệu được tải xong
        splashScreen.setKeepOnScreenCondition(() -> !isDataReady.get());

        // Bắt đầu quá trình tải dữ liệu (thay thế bằng gọi API, load database, v.v. trong dự án thực tế)
        loadData();

        // (Tùy chọn) Tùy chỉnh hoạt ảnh thoát của Splash Screen nếu cần
//        customizeSplashScreenExit(splashScreen);

        Log.d(TAG, "onCreate finished");

//        dialog_signUp_Option();
//        dialog_signIn_Option();
        startActivity(new Intent(MainActivity.this, TaskActivity.class));
    }

    /**
     * Phương thức tải dữ liệu bất đồng bộ.
     * Ở đây dùng Handler để mô phỏng, nhưng bạn nên dùng các thư viện/hướng tiếp cận hiện đại
     * (ví dụ: Retrofit, LiveData, RxJava, hoặc Kotlin coroutines) khi triển khai dự án thực tế.
     */
    private void loadData() {
        Log.d(TAG, "Starting data loading process...");
        // Mô phỏng tải dữ liệu trong 2 giây
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Log.d(TAG, "Data loaded successfully.");
            isDataReady.set(true);
        }, 2000);
    }

    /**
     * Tùy chỉnh hoạt ảnh thoát của splash screen.
     * Phương thức này tạo hiệu ứng fade-out cho toàn bộ màn hình splash.
     * Nếu không cần thiết, bạn có thể bỏ qua việc này để sử dụng hiệu ứng mặc định.
     */
    private void customizeSplashScreenExit(@NonNull SplashScreen splashScreen) {
        splashScreen.setOnExitAnimationListener(splashScreenViewProvider -> {
            Log.d(TAG, "Splash screen exit animation started.");
            final View splashView = splashScreenViewProvider.getView();

            // Tạo hoạt ảnh fade-out
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(splashView, View.ALPHA, 1f, 0f);
            fadeOut.setDuration(300);
            fadeOut.setInterpolator(new AnticipateInterpolator());
            fadeOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    splashScreenViewProvider.remove();
                }
            });
            fadeOut.start();
        });
    }
    private void dialog_signUp_Option() {
        btn_signUp.setOnClickListener(v -> showDialogOptionSignIn_Up(ACTION_SIGN_UP));
    }

    private void dialog_signIn_Option() {
        btn_signIn.setOnClickListener(v -> showDialogOptionSignIn_Up(ACTION_SIGN_IN));
    }

    private void showDialogOptionSignIn_Up(String action) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this, R.style.CustomDialogTheme);
        alertDialog.setTitle(action.equals(ACTION_SIGN_UP) ? "Đăng ký" : "Đăng nhập");
        alertDialog.setIcon(R.drawable.trello_logo_vector);

        // 1. Tạo danh sách dữ liệu các lựa chọn
        List<DialogOptionItem> optionsList = new ArrayList<>();
        String baseText = action.equals(ACTION_SIGN_UP) ? "Đăng ký" : "Đăng nhập";

        // Thêm lựa chọn Email (Thay R.drawable.ic_email bằng icon thật của bạn)
        optionsList.add(new DialogOptionItem(R.drawable.ic_email_placeholder, baseText + " bằng Email"));
        // Thêm lựa chọn Google (Thay R.drawable.ic_google bằng icon thật của bạn)
        optionsList.add(new DialogOptionItem(R.drawable.ic_google_placeholder, baseText + " bằng tài khoản Google"));

        // 2. Tạo instance của Adapter tùy chỉnh
        DialogOptionAdapter adapter = new DialogOptionAdapter(this, optionsList);

        // 3. Sử dụng setAdapter thay vì setSingleChoiceItems
        // Listener sẽ được gọi khi một item được nhấn
        alertDialog.setAdapter(adapter, (dialog, which) -> {
            try {
                if (which == 0) { // Lựa chọn Email
                    Intent intent = new Intent(MainActivity.this,
                            action.equals(ACTION_SIGN_UP) ? RegisterActivity.class : LoginActivity.class);
                    startActivity(intent);
                } else if (which == 1) { // Lựa chọn Google
                    signInWithGoogle();
                }
            } catch (Exception e) {
                Log.e("DialogOption", "Error handling dialog click", e); // Log lỗi chi tiết hơn
                Toast.makeText(MainActivity.this, "Đã xảy ra lỗi", Toast.LENGTH_SHORT).show();
            }
            // Không cần gọi dialog.dismiss() ở đây, vì setAdapter tự động dismiss khi item được chọn
        });

        // Giữ lại nút Huỷ nếu bạn muốn
        alertDialog.setNegativeButton("Huỷ", (dialog, which) -> {
            // Toast.makeText(MainActivity.this, "Đã huỷ", Toast.LENGTH_SHORT).show(); // Có thể bỏ toast này nếu không cần thiết
        });

        alertDialog.create().show();
    }

    private void signInWithGoogle() {
        Toast.makeText(this, "Đăng nhập bằng Google (chưa triển khai)", Toast.LENGTH_SHORT).show();
        // Thêm logic Google Sign-In nếu cần
    }


    // Custom Dialog option Item
    public class DialogOptionItem {
        private int iconResId; // Resource ID của icon
        private String text;   // Nội dung text

        public DialogOptionItem(int iconResId, String text) {
            this.iconResId = iconResId;
            this.text = text;
        }

        public int getIconResId() {
            return iconResId;
        }

        public String getText() {
            return text;
        }
    }
    public class DialogOptionAdapter extends ArrayAdapter<DialogOptionItem> {

        public DialogOptionAdapter(@NonNull Context context, @NonNull List<DialogOptionItem> options) {
            // Truyền 0 cho resource ID vì chúng ta sẽ inflate layout tùy chỉnh
            super(context, 0, options);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // Lấy item dữ liệu cho vị trí này
            DialogOptionItem optionItem = getItem(position);

            // Kiểm tra xem view đã có sẵn chưa, nếu không thì inflate layout mới
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_dialog_option, parent, false);
            }

            // Lấy các view thành phần từ layout
            ImageView iconImageView = convertView.findViewById(R.id.option_icon_iv);
            TextView textView = convertView.findViewById(R.id.option_text_tv);

            // Đổ dữ liệu vào các view
            if (optionItem != null) {
                iconImageView.setImageResource(optionItem.getIconResId());
                textView.setText(optionItem.getText());
            }

            // Trả về view hoàn chỉnh để hiển thị
            return convertView;
        }
    }
}

