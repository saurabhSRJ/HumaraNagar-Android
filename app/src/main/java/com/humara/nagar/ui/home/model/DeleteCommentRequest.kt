package com.humara.nagar.ui.home.model

import com.google.gson.annotations.SerializedName

data class DeleteCommentRequest(
    @SerializedName("comment_id") val commentId: Long,
)