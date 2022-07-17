package com.bagashyt.myintermediate.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagashyt.myintermediate.data.remote.AuthRepository
import com.bagashyt.myintermediate.data.remote.StoryRepository
import com.bagashyt.myintermediate.data.remote.response.StoriesResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val storyRepository: StoryRepository
) : ViewModel() {

    fun saveAuthToken(token: String) {
        viewModelScope.launch {
            authRepository.saveAuthToken(token)
        }
    }

    suspend fun getAllStories(token: String): Flow<Result<StoriesResponse>> =
        storyRepository.getAllStories(token, null, null)
}