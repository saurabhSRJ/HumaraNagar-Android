package com.humara.nagar.ui.signup.model

import com.google.gson.annotations.SerializedName

data class UserInfo(
    @SerializedName("name") val name: String,
    @SerializedName("father_spouse_name") val fatherSpouseName: String,
    @SerializedName("image") val image: String?,
    @SerializedName("gender") val gender: String,
    @SerializedName("ward") val ward: String,
    @SerializedName("role") val role: String,
    @SerializedName("date_of_birth") val dateOfBirth: String
) {
    fun getUserObjectFromUserInfo(userId: Long, mobileNumber: String): User {
        return User(
            userId = userId,
            mobileNumber = mobileNumber,
            name = name,
            fatherOrSpouseName = fatherSpouseName,
            image = image,
            gender = gender,
            ward = ward,
            role = role,
            dateOfBirth = dateOfBirth
        )
    }
}
