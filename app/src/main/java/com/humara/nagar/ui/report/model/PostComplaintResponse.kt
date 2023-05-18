package com.humara.nagar.ui.report.model

import com.google.gson.annotations.SerializedName

data class PostComplaintResponse(
    @SerializedName("complaint_id") var complaintId: String
)
