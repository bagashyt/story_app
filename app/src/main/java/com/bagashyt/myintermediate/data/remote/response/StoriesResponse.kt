package com.bagashyt.myintermediate.data.remote.response

import com.bagashyt.myintermediate.data.model.StoryModel
import com.google.gson.annotations.SerializedName

data class StoriesResponse(

    @field:SerializedName("listStory")
    val stories: List<StoryModel>,
    @field:SerializedName("error")
    val error: Boolean,
    @field:SerializedName("message")
    val message: String
)

