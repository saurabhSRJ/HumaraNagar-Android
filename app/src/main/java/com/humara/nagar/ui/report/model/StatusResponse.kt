package com.humara.nagar.ui.report.model

import com.google.gson.annotations.SerializedName


data class StatusResponse(
    @SerializedName("status") var status : Boolean
)