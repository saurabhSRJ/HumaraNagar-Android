package com.humara.nagar.ui.residents.model

import com.google.gson.annotations.SerializedName


data class Residents(
    @SerializedName("name") var name: String? = null,
    @SerializedName("fathers_name") var fathersName: String? = null,
    @SerializedName("spouse_name") var spouseName: String? = null,
    @SerializedName("gender") var gender: String? = null,
    @SerializedName("locality") var locality: String? = null,
    @SerializedName("house_number") var houseNumber: String? = null,
    @SerializedName("voter_id") var voterId: String? = null,
    @SerializedName("age") var age: Int? = null,
    @SerializedName("phone_number") var phoneNumber: String? = null
)