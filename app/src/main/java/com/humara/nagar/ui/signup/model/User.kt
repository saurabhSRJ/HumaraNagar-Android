package com.humara.nagar.ui.signup.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("user_id") val userId: Long = 0,
    @SerializedName("name") var name: String = "",
    @SerializedName("mobile_number") val mobileNumber: String,
    @SerializedName("parent_name") var parentName: String = "",
    @SerializedName("date_of_birth") var dateOfBirth: String = "",
    @SerializedName("gender") var gender: String = "",
    @SerializedName("ward_number") var wardNumber: String = ""
)
