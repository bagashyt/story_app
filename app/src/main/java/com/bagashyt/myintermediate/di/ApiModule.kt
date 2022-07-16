package com.bagashyt.myintermediate.di

import com.bagashyt.myintermediate.data.remote.retrofit.ApiConfig
import com.bagashyt.myintermediate.data.remote.retrofit.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {
    @Provides
    @Singleton
    fun provideApiService(): ApiService = ApiConfig.getApiService()
}