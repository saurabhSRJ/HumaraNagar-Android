package com.humara.nagar.ui.signup.model

import com.google.gson.annotations.SerializedName

data class LogoutRequest(
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("refresh_token") val refreshToken: String
)
