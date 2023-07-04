package com.humara.nagar.ui.home.create_post.model

import com.google.gson.annotations.SerializedName

data class TextPostRequest(
    @SerializedName("caption") val caption: String
)
