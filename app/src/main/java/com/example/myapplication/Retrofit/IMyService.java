package com.example.myapplication.Retrofit;

import io.reactivex.Observable;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface IMyService {
    @POST("register")
    @FormUrlEncoded
    Observable<String> registerUser(@Field("phonenumber") String phonenumber,
                                    @Field("ID") String ID,
                                    @Field("name") String name);

    @POST("login")
    @FormUrlEncoded
    Observable<String> loginUser(@Field("ID") String ID,
                                 @Field("name") String name);
}
