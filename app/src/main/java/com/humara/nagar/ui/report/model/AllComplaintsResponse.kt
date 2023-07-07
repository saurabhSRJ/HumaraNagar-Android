package com.humara.nagar.ui.report.model

import com.google.gson.annotations.SerializedName

data class AllComplaintsResponse(
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("total_resolved") val totalResolved: Int,
    @SerializedName("total_pending") val totalPending: Int,
    @SerializedName("complaints") var complaints: ArrayList<ComplaintDetails>? = null
)