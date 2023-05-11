package com.humara.nagar.network.retrofit

import com.google.gson.annotations.SerializedName

data class TokenRefreshRequest(
    @SerializedName("user_id") val userId: Long,
    @SerializedName("refresh_token") val refreshToken: String
)
