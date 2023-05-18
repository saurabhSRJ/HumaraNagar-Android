package com.humara.nagar.ui.signup.signup_or_login.model

import com.google.gson.annotations.SerializedName

data class SendOtpRequest(
    @SerializedName("phone_number") val phoneNumber: String
)
