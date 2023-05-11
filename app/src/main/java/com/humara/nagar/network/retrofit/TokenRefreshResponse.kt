package com.humara.nagar.network.retrofit

import com.google.gson.annotations.SerializedName

data class TokenRefreshResponse(
    @SerializedName("token") val token: String,
    @SerializedName("refresh_token") val refreshToken: String
)
