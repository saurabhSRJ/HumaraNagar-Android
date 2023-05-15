package com.humara.nagar.ui.signup.model

import com.google.gson.annotations.SerializedName

data class UserReferenceDataResponse(
    @SerializedName("localities") val localities: ArrayList<LocalityDetail>,
    @SerializedName("categories") val categories: ArrayList<CategoryDetails>
)

data class LocalityDetail(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("ward_id") val wardId: Long
)

data class CategoryDetails(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String
)
