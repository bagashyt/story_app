package com.bagashyt.myintermediate.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("remote_keys")
data class RemoteKeys(
    @PrimaryKey
    val id: String,
    val nextKey: Int?,
    val prevKey: Int?
)
