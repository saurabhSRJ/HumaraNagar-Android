package com.humara.nagar.ui.residents.model

import com.google.gson.annotations.SerializedName

data class GetResidentsRequest(
    @SerializedName("wards_filter") val wardsFilter: List<Int>
)

