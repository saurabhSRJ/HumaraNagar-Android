package com.humara.nagar.ui.signup.model

/**
 * Data class representing saved user profile in shared preference after user has successfully logged-in or signed-up.
 * Note: userId, mobileNumber, ward, roleId and role of the user is directly available in user preference. No need to fetch user profile for these values.
 */
data class User(
    val userId: Long,
    val name: String,
    val mobileNumber: String,
    val fatherOrSpouseName: String,
    val gender: String,
    val image: String?,
    val role: String,
    val ward: String,
    val dateOfBirth: String
)
