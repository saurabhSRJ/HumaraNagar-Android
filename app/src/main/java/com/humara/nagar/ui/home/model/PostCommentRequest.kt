package com.humara.nagar.ui.home.model

import com.google.gson.annotations.SerializedName

data class PostCommentRequest(
    @SerializedName("comment") val comment: String
)
