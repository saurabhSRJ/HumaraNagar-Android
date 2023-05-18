package com.humara.nagar.ui.signup.model

import com.google.gson.annotations.SerializedName

data class UserReferenceDataRequest(
    @SerializedName("user_id") val userId: Long
)
