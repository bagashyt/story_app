package com.bagashyt.myintermediate.ui.add

import androidx.lifecycle.ViewModel
import androidx.paging.ExperimentalPagingApi
import com.bagashyt.myintermediate.data.remote.AuthRepository
import com.bagashyt.myintermediate.data.remote.StoryRepository
import com.bagashyt.myintermediate.data.remote.response.StoryUploadResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
@ExperimentalPagingApi
class AddStoryViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val storyRepository: StoryRepository
) : ViewModel() {

    fun getAuthToken(): Flow<String?> = authRepository.getAuthToken()

    suspend fun uploadImage(
        token: String,
        file: MultipartBody.Part,
        description: RequestBody
    ): Flow<Result<StoryUploadResponse>> =
        storyRepository.uploadImage(token, file, description)
}