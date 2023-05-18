package com.humara.nagar.ui.signup.profile_creation.model

import com.google.gson.annotations.SerializedName

data class ProfileCreationRequest(
    @SerializedName("user_id") val userId: Long,
    @SerializedName("name") var name: String,
    @SerializedName("father_or_spouse_name") var fatherOrSpouseName: String,
    @SerializedName("date_of_birth") var dateOfBirth: String,
    @SerializedName("gender") var gender: String,
    @SerializedName("locality") var locality: String
)
