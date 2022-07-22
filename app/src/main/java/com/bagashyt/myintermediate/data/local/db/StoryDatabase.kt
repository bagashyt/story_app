package com.bagashyt.myintermediate.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bagashyt.myintermediate.data.model.StoryModel

@Database(
    entities = [StoryModel::class, RemoteKeys::class],
    version = 1,
    exportSchema = false
)
abstract class StoryDatabase : RoomDatabase(){
    abstract fun storyDao(): StoryDao
    abstract fun remoteKeysDao(): RemoteKeysDao

}
