package com.bagashyt.myintermediate.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagashyt.myintermediate.data.remote.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {

    suspend fun userLogin(email: String, password: String) =
        authRepository.userLogin(email, password)

    fun saveAuthToken(token: String){
        viewModelScope.launch {
            authRepository.saveAuthToken(token)
        }
    }
}