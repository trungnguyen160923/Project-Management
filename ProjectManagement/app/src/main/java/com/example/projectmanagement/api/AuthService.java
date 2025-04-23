package com.example.projectmanagement.api;

import com.example.projectmanagement.data.model.User;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface AuthService {

    // Ví dụ endpoint "login" (thay thế bằng đường dẫn và tham số thực tế)
    @FormUrlEncoded
    @POST("login")
    Call<User> loginUser(
            @Field("email") String email,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("register")
    Call<User> registerUser(
            @Field("email") String email,
            @Field("fullname") String fullname,
            @Field("password") String password

    );
}

