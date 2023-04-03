package com.humara.nagar.ui.signup.signup_or_login.model

import com.google.gson.annotations.SerializedName

data class SendOtpResponse(
    @SerializedName("passcode") val passcode: String?,
    @SerializedName("is_eligible_to_login") val isEligibleToLogin: Boolean
)
