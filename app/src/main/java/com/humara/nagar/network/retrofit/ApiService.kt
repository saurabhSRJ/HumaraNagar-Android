package com.humara.nagar.network.retrofit

import com.humara.nagar.network.NetworkResponse
import com.humara.nagar.ui.report.model.*
import com.humara.nagar.ui.residents.model.FiltersResponse
import com.humara.nagar.ui.residents.model.Residents
import com.humara.nagar.ui.residents.model.ResidentsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("users")
    suspend fun getUsers(): NetworkResponse<List<User>>

    @GET("16521ae4-340f-4119-936d-ad4d21996dce")
    suspend fun getComplaintStatus(): NetworkResponse<ComplaintStatus>

    @GET("b9954a2e-085b-42d0-9385-ab65d4e88967")
    suspend fun getAllComplaints(): NetworkResponse<AllComplaintsResponse>

    @POST("03904b13-f08c-4fcd-8d06-ad8dba223e99")
    suspend fun postReportComplaint(@Body complaintsRequest: ComplaintsRequest): NetworkResponse<ComplaintIDResponse>

    @POST("191a7744-3474-4b20-9884-5e81a3b791db")
    suspend fun requestAcknowledge(@Query("id") id: String, @Body comment: String): NetworkResponse<StatusResponse>

    @POST("191a7744-3474-4b20-9884-5e81a3b791db")
    suspend fun requestFinish(@Query("id") id: String, @Body comment: String): NetworkResponse<StatusResponse>

    @POST("191a7744-3474-4b20-9884-5e81a3b791db")
    suspend fun requestWithdraw(@Query("id") id: String, @Body comment: String): NetworkResponse<StatusResponse>

    @POST("191a7744-3474-4b20-9884-5e81a3b791db")
    suspend fun requestRating(@Query("id") id: String, @Body rating: Int): NetworkResponse<StatusResponse>

    @GET("304c90c8-d6bb-470b-b4bf-81c24a930705")
    suspend fun requestAllResidentsList(): NetworkResponse<ResidentsResponse>

    @GET("692c7a18-12fe-4a99-a90e-a6874ee16b17")
    suspend fun requestAllFilters(): NetworkResponse<FiltersResponse>
}