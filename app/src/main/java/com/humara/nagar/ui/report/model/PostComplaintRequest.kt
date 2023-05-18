package com.humara.nagar.ui.report.model

data class PostComplaintRequest(
    val category: String,
    val locality: String,
    val user_id: Long,
    val location: String,
    val comments: String,
    val location_latitude: Double?,
    val location_longitude: Double?,
)
