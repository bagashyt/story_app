package com.bagashyt.myintermediate.data.paging

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.bagashyt.myintermediate.data.local.db.RemoteKeys
import com.bagashyt.myintermediate.data.local.db.StoryDatabase
import com.bagashyt.myintermediate.data.model.StoryModel
import com.bagashyt.myintermediate.data.remote.retrofit.ApiService

@ExperimentalPagingApi
class StoryRemoteMediator(
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService,
    private val token: String
) : RemoteMediator<Int, StoryModel>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, StoryModel>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosesToCurrentItem(state)
                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey ?: return MediatorResult.Success(
                    endOfPaginationReached = remoteKeys != null
                )
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.prevKey ?: return MediatorResult.Success(
                    endOfPaginationReached = remoteKeys != null
                )
                nextKey
            }

        }
        return try {
            val response = apiService.getAllStories(
                location = 0,
                page = page,
                size = state.config.pageSize,
                token = token
            )
            val responseData = response.stories
            val endOfPaginationReached = responseData.isEmpty()

<<<<<<< HEAD
            Log.d("TAG_StoryRemoteMediator", "inserting: $response")
=======
>>>>>>> 479ac96... delete log

            storyDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    storyDatabase.remoteKeysDao().deleteAllRemoteKeys()
                    storyDatabase.storyDao().deleteAllStories()
                }

                val nextKey = if (endOfPaginationReached) null else page + 1
                val prevKey = if (page == INITIAL_PAGE_INDEX) null else page - 1
                val keys = responseData.map {
                    RemoteKeys(
                        id = it.id,
                        prevKey = prevKey,
                        nextKey = nextKey
                    )
                }
                storyDatabase.remoteKeysDao().insertAll(keys)
                storyDatabase.storyDao().insertStory(responseData)
            }
            MediatorResult.Success(endOfPaginationReached)
        } catch (e: Exception) {
            Log.d("TAG_exception","error: $e")
            MediatorResult.Error(e)
        }
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, StoryModel>): RemoteKeys? {
        return state.pages.firstOrNull() { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
            storyDatabase.remoteKeysDao().getRemoteKey(data.id)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, StoryModel>): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
            storyDatabase.remoteKeysDao().getRemoteKey(data.id)
        }
    }

    private suspend fun getRemoteKeyClosesToCurrentItem(state: PagingState<Int, StoryModel>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                storyDatabase.remoteKeysDao().getRemoteKey(id)
            }
        }
    }


    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
}
