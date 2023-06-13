package com.humara.nagar.ui.report.model

import com.google.gson.annotations.SerializedName
import com.humara.nagar.utils.StringUtils

data class ComplaintDetails(
    @SerializedName("complaint_id") val complaintId: String,
    @SerializedName("state") val state: String,
    @SerializedName("category") val category: String? = null,
    @SerializedName("locality") val locality: String? = null,
    @SerializedName("resolved_on") val resolvedOn: String? = null,
    @SerializedName("resolution_expected_on") val resolutionExpectedOn: String? = null,
    @SerializedName("rating") val rating: Int? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("resident_name") val residentName: String? = null
) {
    fun isRatingPresent(): Boolean {
        return (rating ?: 0) > 0
    }

    fun getImageList() = StringUtils.convertToList(image)
}