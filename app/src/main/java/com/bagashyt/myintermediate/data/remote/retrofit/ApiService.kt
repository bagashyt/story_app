package com.bagashyt.myintermediate.data.remote.retrofit

import com.bagashyt.myintermediate.data.remote.response.LoginResponse
import com.bagashyt.myintermediate.data.remote.response.RegisterResponse
import com.bagashyt.myintermediate.data.remote.response.StoriesResponse
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("login")
    suspend fun userLogin(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @FormUrlEncoded
    @POST("register")
    suspend fun userRegister(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): RegisterResponse

    @GET("stories")
    suspend fun getAllStories(
        @Header("Authorization") token: String,
        @Query("page") page: Int?,
        @Query("size") size: Int?
    ): StoriesResponse

}