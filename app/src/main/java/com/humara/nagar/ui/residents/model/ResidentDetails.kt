package com.humara.nagar.ui.residents.model

import com.google.gson.annotations.SerializedName

data class ResidentDetails(
    @SerializedName("name") val name: String? = null,
    @SerializedName("father_spouse_name") val fathersName: String? = null,
    @SerializedName("date_of_birth") val dateOfBirth: String?,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("image") val image: String?,
    @SerializedName("role") val role: String,
    @SerializedName("ward") val ward: String
)