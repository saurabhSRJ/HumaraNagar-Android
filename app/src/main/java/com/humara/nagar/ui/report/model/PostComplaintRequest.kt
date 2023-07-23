package com.humara.nagar.ui.report.model

data class PostComplaintRequest(
    val category_id: Int,
    val ward_id: Int,
    val location: String,
    val comments: String,
    val location_latitude: Double,
    val location_longitude: Double,
)
