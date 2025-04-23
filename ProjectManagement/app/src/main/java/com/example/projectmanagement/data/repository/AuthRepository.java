package com.example.projectmanagement.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.projectmanagement.api.ApiClient;
import com.example.projectmanagement.api.AuthService;
import com.example.projectmanagement.data.model.User;

/**
 * Xử lý logic gọi API cho tác vụ đăng nhập
 */
public class AuthRepository {
    private static AuthRepository instance;
    private AuthService apiService;

    // Constructor private để áp dụng Singleton
    private AuthRepository() {
        apiService = ApiClient.getClient().create(AuthService.class);
    }

    // Lấy instance duy nhất
    public static synchronized AuthRepository getInstance() {
        if (instance == null) {
            instance = new AuthRepository();
        }
        return instance;
    }

    /**
     * Hàm login gọi API, trả về MutableLiveData<User>
     * - Nếu thành công: giá trị user != null
     * - Nếu thất bại: user = null (hoặc bạn có thể trả về thêm dữ liệu khác)
     */
    public MutableLiveData<User> login(String email, String password) {
        MutableLiveData<User> userData = new MutableLiveData<>();

//        apiService.loginUser(email, password).enqueue(new Callback<User>() {
//            @Override
//            public void onResponse(Call<User> call, Response<User> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    // Đăng nhập thành công, server trả về User
//                    userData.setValue(response.body());
//                } else {
//                    // Đăng nhập thất bại, trả về null hoặc xử lý lỗi
//                    userData.setValue(null);
//                }
//            }
//
//            @Override
//            public void onFailure(Call<User> call, Throwable t) {
//                // Lỗi kết nối hoặc ngoại lệ khác
//                userData.setValue(null);
//            }
//        });

        if (email.equals("trung@gmail.com") && password.equals("123456")) {
            // Tạo đối tượng User mẫu (hoặc LoggedInUserView tùy theo thiết kế của bạn)
            User user = new User();
            user.setId(1);
            user.setUsername("test@example.com");
            user.setEmail("test@example.com");
            user.setFullname("Test User");
            // Các thuộc tính khác nếu cần...

            userData.setValue(user);
        } else {
            // Nếu đăng nhập không đúng, trả về null
            userData.setValue(null);
        }

        return userData;
    }

    /**
     * Hàm register gọi API đăng ký, trả về MutableLiveData<User>
     * - Nếu đăng ký thành công: giá trị User != null
     * - Nếu thất bại: User = null (hoặc bạn có thể trả về thêm thông tin lỗi)
     */
    public MutableLiveData<User> register(String email, String fullname, String password) {
        MutableLiveData<User> userData = new MutableLiveData<>();

        // Gọi API đăng ký
//        apiService.registerUser(email, fullname, password).enqueue(new Callback<User>() {
//            @Override
//            public void onResponse(Call<User> call, Response<User> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    // Đăng ký thành công
//                    userData.setValue(response.body());
//                } else {
//                    // Đăng ký thất bại
//                    userData.setValue(null);
//                }
//            }
//
//            @Override
//            public void onFailure(Call<User> call, Throwable t) {
//                // Lỗi kết nối hoặc ngoại lệ khác
//                userData.setValue(null);
//            }
//        });

        // Nếu bạn muốn demo mà chưa có API, có thể dùng hard-code như sau:
         if(email.equals("t@g.c")) {
             User user = new User();
             user.setId(2);
             user.setEmail(email);
             user.setFullname(fullname);
             userData.setValue(user);
         } else {
             userData.setValue(null);
         }

        return userData;
    }
}
