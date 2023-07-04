package com.humara.nagar.ui.signup.profile_creation.model

import com.google.gson.annotations.SerializedName

data class ProfileCreationRequest(
    @SerializedName("user_id") val userId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("father_or_spouse_name") val fatherOrSpouseName: String,
    @SerializedName("date_of_birth") val dateOfBirth: String,
    @SerializedName("gender_id") val genderId: Int,
    @SerializedName("ward_id") val wardId: Int
)
