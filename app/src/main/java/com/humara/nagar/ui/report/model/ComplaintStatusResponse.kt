package com.humara.nagar.ui.report.model

import com.google.gson.annotations.SerializedName
import com.humara.nagar.utils.ComplaintsUtils

/*  #2
    Type: GET
    Endpoint : /complaint/:id
*/

data class ComplaintStatus(
    @SerializedName("current_state") var currentState: String,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("resident_name") val residentName: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("comments") val comments: String? = null,
    @SerializedName("images") val images: ArrayList<String> = arrayListOf(),
    @SerializedName("category") val category: String? = null,
    @SerializedName("locality") val locality: String? = null,
    @SerializedName("tracking_info") val trackingInfo: TrackingInfo? = null,
    @SerializedName("rating") val rating: Int = 0
) {
    fun showRatingSection(): Boolean {
        return currentState == ComplaintsUtils.ComplaintState.RESOLVED.currentState || currentState == ComplaintsUtils.ComplaintState.WITHDRAWN.currentState
    }
}

data class TrackingInfo(
    @SerializedName("states") var states: ArrayList<States> = arrayListOf()
)

data class States(
    @SerializedName("state_text") val stateText: String? = null,
    @SerializedName("state_subtext") val stateSubtext: String? = null,
    @SerializedName("state_comment") val stateComment: String? = null,
    @SerializedName("is_finished") val isFinished: Boolean = false
)