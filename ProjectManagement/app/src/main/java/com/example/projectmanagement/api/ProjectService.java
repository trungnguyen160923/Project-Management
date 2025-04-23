package com.example.projectmanagement.api;

import com.example.projectmanagement.data.model.User;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
public interface ProjectService {
    @FormUrlEncoded
    @POST("createProject")
    Call<User> createProject(
            @Field("projectName") String projectName,
            @Field("backgroundImage") String backgroundImage
    );
}
