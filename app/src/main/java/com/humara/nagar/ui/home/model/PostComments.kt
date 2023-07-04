package com.humara.nagar.ui.home.model

import com.google.gson.annotations.SerializedName

data class PostComments(
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("comments") val comments: List<CommentDetails> = arrayListOf()
)

data class CommentDetails(
    @SerializedName("name") val name: String,
    @SerializedName("image") val image: String?,
    @SerializedName("comment_id") val commentId: Long,
    @SerializedName("comment") val comment: String,
    @SerializedName("created_at") val createdAt: String
)
