package com.humara.nagar.ui.report.model

import com.google.gson.annotations.SerializedName
import com.humara.nagar.utils.StringUtils

data class ComplaintDetails(
    @SerializedName("complaint_id") var complaintId: String,
    @SerializedName("state") var state: String,
    @SerializedName("category") var category: String? = null,
    @SerializedName("locality") var locality: String? = null,
    @SerializedName("resolved_on") var resolvedOn: String? = null,
    @SerializedName("resolution_expected_on") var resolutionExpectedOn: String? = null,
    @SerializedName("rating") var rating: Int? = null,
    @SerializedName("image") var image: String? = null
) {
    fun isRatingPresent(): Boolean {
        return (rating ?: 0) > 0
    }

    fun getImageList() = StringUtils.convertToList(image)
}