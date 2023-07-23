package com.humara.nagar.ui.report.model

import com.google.gson.annotations.SerializedName
import com.humara.nagar.utils.ComplaintsUtils

data class ComplaintStatus(
    @SerializedName("state") var currentState: String,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("resident_name") val residentName: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("comments") val comments: String? = null,
    @SerializedName("image") val images: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("ward") val ward: String? = null,
    @SerializedName("profile_image") val profileImage: String? = null,
    @SerializedName("location_latitude") val latitude: String? = null,
    @SerializedName("location_longitude") val longitude: String? = null,
    @SerializedName("tracking_info") val trackingInfo: List<TrackingInfo> = listOf(),
    @SerializedName("rating") val rating: Int = 0
) {
    fun showRatingSection(): Boolean {
        return currentState == ComplaintsUtils.ComplaintState.RESOLVED.currentState || currentState == ComplaintsUtils.ComplaintState.WITHDRAWN.currentState
    }
}

data class TrackingInfo(
    @SerializedName("state_text") val stateText: String? = null,
    @SerializedName("initial_date") val initialDate: String? = null,
    @SerializedName("state_comment") val stateComment: String? = null,
    @SerializedName("update_date") val updateDate: String? = null,
    @SerializedName("is_finished") val isFinished: Boolean = false
)