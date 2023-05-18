package com.humara.nagar.ui.signup.otp_verification.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("passcode") val passcode: String,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("otp") val otp: String
)
