package com.humara.nagar.ui.signup.otp_verification.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("is_new_user") val isNewUser: Boolean,
    @SerializedName("user_id") val userId: Long
)