package com.humara.nagar.ui.signup.model

import com.google.gson.annotations.SerializedName

data class AppConfigRequest(
    @SerializedName("phone_number") val phoneNumber: String
)
