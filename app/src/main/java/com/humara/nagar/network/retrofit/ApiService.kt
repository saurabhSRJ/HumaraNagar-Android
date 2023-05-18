package com.humara.nagar.network.retrofit

import com.humara.nagar.fcm.FcmTokenRequest
import com.humara.nagar.network.NetworkResponse
import com.humara.nagar.ui.report.model.*
import com.humara.nagar.ui.residents.model.FiltersResponse
import com.humara.nagar.ui.residents.model.ResidentsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("304c90c8-d6bb-470b-b4bf-81c24a930705")
    suspend fun requestAllResidentsList(): NetworkResponse<ResidentsResponse>

    @GET("692c7a18-12fe-4a99-a90e-a6874ee16b17")
    suspend fun requestAllFilters(): NetworkResponse<FiltersResponse>

    @POST("fcm-token-api-endpoint")
    suspend fun updateFcmTokenToServer(@Body request: FcmTokenRequest): NetworkResponse<StatusResponse>
}