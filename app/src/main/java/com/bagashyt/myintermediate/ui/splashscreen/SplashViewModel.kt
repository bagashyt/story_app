package com.bagashyt.myintermediate.ui.splashscreen

import androidx.lifecycle.ViewModel
import com.bagashyt.myintermediate.data.remote.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(private val authRepository: AuthRepository) :
    ViewModel() {

    fun getAuthToken(): Flow<String?> = authRepository.getAuthToken()
}