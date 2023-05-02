package com.humara.nagar.ui.residents.model

import com.google.gson.annotations.SerializedName


data class FiltersResponse(
    @SerializedName("filters") var filters: ArrayList<String> = arrayListOf()
)
