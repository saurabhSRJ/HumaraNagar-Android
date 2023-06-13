package com.humara.nagar.ui.home.model

import com.google.gson.annotations.SerializedName

data class EditPostRequest(
    @SerializedName("caption") val caption: String
)
