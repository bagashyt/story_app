package com.bagashyt.myintermediate.ui.location

import androidx.lifecycle.ViewModel
import androidx.paging.ExperimentalPagingApi
import com.bagashyt.myintermediate.data.remote.AuthRepository
import com.bagashyt.myintermediate.data.remote.StoryRepository
import com.bagashyt.myintermediate.data.remote.response.StoriesResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
@ExperimentalPagingApi
class LocationViewModel @Inject constructor(
    private val storyRepository: StoryRepository,
    private val authRepository: AuthRepository
) :
    ViewModel() {
    suspend fun getListStories(token: String): Flow<Result<StoriesResponse>> =
        storyRepository.getAllStories(token)

    fun getAuthToken(): Flow<String?> = authRepository.getAuthToken()
}