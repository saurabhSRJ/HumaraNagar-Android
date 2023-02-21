package com.humara.nagar.ui.report.model

import com.google.gson.annotations.SerializedName

/*  #2
    Type: GET
    Endpoint : /complaint/:id
*/

data class ComplaintStatus(
    @SerializedName("phone_number") val phone_number: String? = null,
    @SerializedName("resident_name") val resident_name: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("comments") val comments: String? = null,
    @SerializedName("images") val images: ArrayList<String> = arrayListOf(),
    @SerializedName("category") val category: String? = null,
    @SerializedName("locality") val locality: String? = null,
    @SerializedName("is_resolved") val is_resolved: Boolean? = null,
    @SerializedName("trackingInfo") val trackingInfo: TrackingInfo? = TrackingInfo(),
    @SerializedName("rating") val rating: Int? = null
)

data class TrackingInfo(
    @SerializedName("current_state") var currentState : String? = null,
    @SerializedName("states") var states: ArrayList<States> = arrayListOf()
)

data class States(
    @SerializedName("state_text") val state_text: String?  = null,
    @SerializedName("state_subtext") val state_subtext: String?  = null,
    @SerializedName("state_comment") val state_comment: String?  = null,
    @SerializedName("Is_finished") val Is_finished: Boolean? = null
)