package com.bagashyt.myintermediate.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.bagashyt.myintermediate.data.local.db.StoryDatabase
import com.bagashyt.myintermediate.data.model.StoryModel
import com.bagashyt.myintermediate.data.paging.StoryRemoteMediator
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

@ExperimentalPagingApi
class StoryRepository @Inject constructor(
    private val apiService: ApiService,
    private val storyDatabase: StoryDatabase
) {


     fun getListStories(token: String): Flow<PagingData<StoryModel>> {
        return Pager(
            config = PagingConfig(pageSize = 10),
            remoteMediator = StoryRemoteMediator(
                apiService = apiService,
                storyDatabase = storyDatabase,
                token = generateBearerToken(token)
            ),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStories()
            }
        ).flow
    }

    suspend fun getAllStories(
        token: String,
        page: Int? = null,
        size: Int? = null,
    ): Flow<Result<StoriesResponse>> = flow {
        try {
            val bearerToken = generateBearerToken(token)
            val response = apiService.getAllStories(bearerToken, 1,  page, size)
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
    ): Flow<Result<StoryUploadResponse>> = flow {
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