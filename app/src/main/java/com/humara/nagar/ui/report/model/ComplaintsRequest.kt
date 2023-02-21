package com.humara.nagar.ui.report.model

import okhttp3.MultipartBody
import retrofit2.http.Part

/*  #1
    Type : POST
	Endpoint : /complaint
*/

data class ComplaintsRequest(
    val category: String,
    val locality: String,
    val phone_number: String,
    val location: String,
    val comments: String,
    @Part val images: List<MultipartBody.Part>
)