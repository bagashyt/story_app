package com.bagashyt.myintermediate.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Entity("story_db")
@Parcelize
data class StoryModel(

    @PrimaryKey
    @field:SerializedName("id")
    val id: String,
    @field:SerializedName("photoUrl")
    val photoUrl: String? = null,
    @field:SerializedName("name")
    val name: String? = null,
    @field:SerializedName("description")
    val description: String? = null,
    @field:SerializedName("lat")
    val lat: Double? = null,
    @field:SerializedName("lon")
    val lon: Double? = null,
) : Parcelable