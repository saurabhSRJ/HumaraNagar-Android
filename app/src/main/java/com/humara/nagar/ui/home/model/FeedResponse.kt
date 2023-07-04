package com.humara.nagar.ui.home.model

import com.google.gson.annotations.SerializedName

data class FeedResponse(
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("posts") val posts: List<Post> = arrayListOf()
)