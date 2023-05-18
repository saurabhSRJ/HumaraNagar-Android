package com.humara.nagar.ui.signup.model

import com.google.gson.annotations.SerializedName

data class AppConfigRequest(
    @SerializedName("user_id") val userId: Long
)
