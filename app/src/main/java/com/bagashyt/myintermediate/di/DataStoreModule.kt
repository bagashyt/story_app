package com.bagashyt.myintermediate.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.bagashyt.myintermediate.data.local.AuthPreferencesDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.datastore: DataStore<Preferences> by preferencesDataStore(name = "application")

@Module
@InstallIn(SingletonComponent::class)
class DataStoreModule {
    @Provides
    fun providerDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.datastore

    @Provides
    @Singleton
    fun provideAuthPreferences(dataStore: DataStore<Preferences>): AuthPreferencesDataSource =
        AuthPreferencesDataSource(dataStore)
}