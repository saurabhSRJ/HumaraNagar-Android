package com.humara.nagar.ui.report.model

import com.google.gson.annotations.SerializedName

/*  #3
    Type: GET
    Endpoint: /complaint/all
*/

data class AllComplaintsResponse(
    @SerializedName("complaints") var complaints: ArrayList<ComplaintsResponse> = arrayListOf()
)