package com.humara.nagar.ui.residents.model

import com.google.gson.annotations.SerializedName

data class ResidentsResponse(
    @SerializedName("totalPages") var totalPages: Int? = null,
    @SerializedName("nextPage") var nextPage: Int? = null,
    @SerializedName("previousPage") var previousPage: Int? = null,
    @SerializedName("Residents") var Residents: ArrayList<Residents> = arrayListOf()
)