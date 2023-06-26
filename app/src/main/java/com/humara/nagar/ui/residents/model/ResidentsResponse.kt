package com.humara.nagar.ui.residents.model

import com.google.gson.annotations.SerializedName

data class ResidentsResponse(
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("Residents") val residentDetails: List<ResidentDetails> = listOf()
)