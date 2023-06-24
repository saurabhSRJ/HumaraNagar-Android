package com.humara.nagar.ui.signup.model

import com.google.gson.annotations.SerializedName

data class AppConfigResponse(
    @SerializedName("role") val role: String,
    @SerializedName("role_id") val roleId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("image") val image: String?
)