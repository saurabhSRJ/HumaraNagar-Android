package com.humara.nagar.ui.add_user.model

import com.google.gson.annotations.SerializedName

data class AddUserDetailsRequest(
    @SerializedName("name") val name: String,
    @SerializedName("father_spouse_name") val fatherOrSpouseName: String,
    @SerializedName("date_of_birth") val dateOfBirth: String,
    @SerializedName("gender_id") val genderId: Int,
    @SerializedName("ward_id") val wardId: Int,
    @SerializedName("role_id") val roleId: Int,
    @SerializedName("phone_number") val mobileNumber: String
)
