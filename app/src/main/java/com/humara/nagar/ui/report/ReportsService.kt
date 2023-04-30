package com.humara.nagar.ui.report

import com.humara.nagar.constants.NetworkConstants
import com.humara.nagar.network.NetworkResponse
import com.humara.nagar.ui.report.model.PostComplaintRequest
import com.humara.nagar.ui.report.model.PostComplaintResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ReportsService {
    @POST(NetworkConstants.NetworkAPIConstants.COMPLAINT)
    suspend fun postComplaint(@Body request: PostComplaintRequest): NetworkResponse<PostComplaintResponse>
}