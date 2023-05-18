package com.humara.nagar.ui.report.model

import com.google.gson.annotations.SerializedName

data class AllComplaintsResponse(
    @SerializedName("complaints") var complaints: ArrayList<ComplaintDetails> = arrayListOf()
)