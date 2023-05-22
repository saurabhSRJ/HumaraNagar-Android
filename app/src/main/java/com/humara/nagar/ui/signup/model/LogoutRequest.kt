package com.humara.nagar.ui.signup.model

import com.google.gson.annotations.SerializedName

data class LogoutRequest(
    @SerializedName("user_id") val userId: Long,
    @SerializedName("refresh_token") val refreshToken: String
)
