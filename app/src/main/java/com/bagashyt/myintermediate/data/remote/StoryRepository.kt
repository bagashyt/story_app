package com.bagashyt.myintermediate.data.remote

import com.bagashyt.myintermediate.data.remote.response.StoriesResponse
import com.bagashyt.myintermediate.data.remote.response.StoryUploadResponse
import com.bagashyt.myintermediate.data.remote.retrofit.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class StoryRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getAllStories(
        token: String,
        page: Int? = null,
        size: Int? = null,
    ): Flow<Result<StoriesResponse>> = flow {
        try {
            val bearerToken = generateBearerToken(token)
            val response = apiService.getAllStories(bearerToken, page, size)
            emit(Result.success(response))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    private fun generateBearerToken(token: String): String {
        return "Bearer $token"
    }

    suspend fun uploadImage(
        token: String,
        file: MultipartBody.Part,
        description: RequestBody
    ): Flow<Result<StoryUploadResponse>> = flow<Result<StoryUploadResponse>> {
        try {
            val bearerToken = generateBearerToken(token)
            val response = apiService.uploadImage(bearerToken, file, description)
            emit(Result.success(response))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }
}