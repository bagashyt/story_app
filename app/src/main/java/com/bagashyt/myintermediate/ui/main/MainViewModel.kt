package com.bagashyt.myintermediate.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.bagashyt.myintermediate.data.local.db.StoryDatabase
import com.bagashyt.myintermediate.data.model.StoryModel
import com.bagashyt.myintermediate.data.remote.AuthRepository
import com.bagashyt.myintermediate.data.remote.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalPagingApi
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val storyRepository: StoryRepository,
) : ViewModel() {

    fun saveAuthToken(token: String) {
        viewModelScope.launch {
            authRepository.saveAuthToken(token)
        }
    }

    fun deleteAuthToken() {
        viewModelScope.launch {
            authRepository.deleteAuthToken()

        }
    }

    fun getListStories(token: String): Flow<PagingData<StoryModel>> =
        storyRepository.getListStories(token).cachedIn(viewModelScope)
}