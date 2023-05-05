package com.humara.nagar.ui.report.model

import com.google.gson.annotations.SerializedName

data class UpdateComplaintRequest(
    @SerializedName("comment") val comment: String
)
