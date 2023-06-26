package com.humara.nagar.ui.residents.model

import com.google.gson.annotations.SerializedName

data class ResidentDetails(
    @SerializedName("name") val name: String? = null,
    @SerializedName("fathers_name") val fathersName: String? = null,
    @SerializedName("age") val age: Int?,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("image") val image: String?,
    @SerializedName("role") val role: String,
    @SerializedName("ward") val ward: String
)