package com.humara.nagar.ui.report.model

import com.google.gson.annotations.SerializedName

data class RateComplaintServiceRequest(
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String?
)
