package com.humara.nagar.ui.add_user.model

import com.google.gson.annotations.SerializedName

data class UserOtpResponse(
    @SerializedName("otp") val otp: String
)
