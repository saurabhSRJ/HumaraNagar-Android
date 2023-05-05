package com.humara.nagar.ui.report.model

import com.google.gson.annotations.SerializedName

/*  #3
    Type: GET
    Endpoint: /complaint/all
*/

data class ComplaintDetails(
    @SerializedName("complaint_id") var complaintId: String,
    @SerializedName("state") var state: String,
    @SerializedName("category") var category: String? = null,
    @SerializedName("locality") var locality: String? = null,
    @SerializedName("resolved_on") var resolvedOn: String? = null,
    @SerializedName("resolution_expected_on") var resolutionExpectedOn: String? = null,
    @SerializedName("rating") var rating: Int? = null
) {
    fun isRatingPresent(): Boolean {
        return (rating ?: 0) > 0
    }
}