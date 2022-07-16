package com.bagashyt.myintermediate.data.remote

import com.bagashyt.myintermediate.data.local.AuthPreferencesDataSource
import com.bagashyt.myintermediate.data.remote.response.LoginResponse
import com.bagashyt.myintermediate.data.remote.retrofit.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val preferencesDataSource: AuthPreferencesDataSource
) {
    suspend fun userLogin(email: String, password: String): Flow<Result<LoginResponse>> = flow {
        try {
            val response = apiService.userLogin(email, password)
            emit(Result.success(response))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun getAuthToken(): Flow<String?> = preferencesDataSource.getAuthToken()

    suspend fun saveAuthToken(token: String) {
        preferencesDataSource.saveAuthToken(token)
    }
}