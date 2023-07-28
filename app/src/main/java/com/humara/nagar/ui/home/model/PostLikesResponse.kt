package com.humara.nagar.ui.home.model

import com.google.gson.annotations.SerializedName

data class PostLikesResponse(
    @SerializedName("is_liked_by_user") val isLikedByUser: Boolean,
    @SerializedName("likes") val likes: List<LikeDetails> = emptyList()
)

data class LikeDetails(
    @SerializedName("user_id") val userId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("image") val image: String?,
    @SerializedName("ward") val ward: String,
    @SerializedName("role") val role: String
)