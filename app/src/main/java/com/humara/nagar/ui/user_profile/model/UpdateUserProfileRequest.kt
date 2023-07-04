package com.humara.nagar.ui.user_profile.model

import com.google.gson.annotations.SerializedName

data class UpdateUserProfileRequest(
    @SerializedName("date_of_birth") val dateOfBirth: String,
    @SerializedName("father_spouse_name") val fatherSpouseName: String,
    @SerializedName("bio") val bio: String? = null,
    @SerializedName("email") val email: String? = null
)
