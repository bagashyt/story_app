package com.bagashyt.myintermediate.data.local.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bagashyt.myintermediate.data.model.StoryModel

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(quote: List<StoryModel>)

    @Query("SELECT * FROM story_db")
    fun getAllStories(): PagingSource<Int, StoryModel>

    @Query("DELETE FROM story_db")
    suspend fun deleteAllStories()
}