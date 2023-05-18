package com.humara.nagar.network

import com.google.gson.annotations.SerializedName

class ApiError @JvmOverloads constructor(val responseCode: Int, val message: String? = null)

data class ErrorResponse(@SerializedName("message") val message: String?)