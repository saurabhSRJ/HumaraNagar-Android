package com.humara.nagar.ui.signup.otp_verification.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("is_new_user") val isNewUser: Boolean,
    @SerializedName("user_id") val userId: Long,
    @SerializedName("user_info") val userInfo: UserInfo? = null
)

data class UserInfo(
    @SerializedName("user_id") val userId: Long,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String = "",
    @SerializedName("locality") val locality: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("father_spouse_name") val fatherOrSpouseName: String
) {
    fun getFullName(): String {
        return "$firstName $lastName".trim()
    }
}
