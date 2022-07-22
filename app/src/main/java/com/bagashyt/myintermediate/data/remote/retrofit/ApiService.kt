package com.bagashyt.myintermediate.data.remote.retrofit

import com.bagashyt.myintermediate.data.remote.response.LoginResponse
import com.bagashyt.myintermediate.data.remote.response.RegisterResponse
import com.bagashyt.myintermediate.data.remote.response.StoriesResponse
import com.bagashyt.myintermediate.data.remote.response.StoryUploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
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
        @Query("location") location: Int? = null,
        @Query("page") page: Int?,
        @Query("size") size: Int?
    ): StoriesResponse

    @Multipart
    @POST("stories")
    suspend fun uploadImage(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody
    ): StoryUploadResponse

}