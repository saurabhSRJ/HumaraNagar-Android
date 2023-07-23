package com.humara.nagar.ui.user_profile.model

import com.google.gson.annotations.SerializedName

data class UserProfileResponse(
    @SerializedName("user_info") val userProfile: UserProfile
)

data class UserProfile(
    @SerializedName("name") val name: String,
    @SerializedName("date_of_birth") val dateOfBirth: String,
    @SerializedName("created_by") val createdBy: String? = null,
    @SerializedName("created_on") val createdOn: String?,
    @SerializedName("father_spouse_name") val fatherSpouseName: String,
    @SerializedName("ward") val ward: String,
    @SerializedName("role") val role: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("bio") val bio: String? = null,
)