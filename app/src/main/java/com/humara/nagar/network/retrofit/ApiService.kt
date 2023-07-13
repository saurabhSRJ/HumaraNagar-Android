package com.humara.nagar.network.retrofit

import com.humara.nagar.constants.NetworkConstants
import com.humara.nagar.fcm.FcmTokenRequest
import com.humara.nagar.network.NetworkResponse
import com.humara.nagar.ui.report.model.*
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST(NetworkConstants.NetworkAPIConstants.FCM_TOKEN)
    suspend fun updateFcmTokenToServer(@Body request: FcmTokenRequest): NetworkResponse<StatusResponse>
}