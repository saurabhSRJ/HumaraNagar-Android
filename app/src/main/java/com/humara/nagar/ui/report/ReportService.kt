package com.humara.nagar.ui.report

import com.humara.nagar.constants.NetworkConstants
import com.humara.nagar.network.NetworkResponse
import com.humara.nagar.ui.report.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ReportService {
    @POST(NetworkConstants.NetworkAPIConstants.COMPLAINT)
    suspend fun postComplaint(@Body request: PostComplaintRequest): NetworkResponse<PostComplaintResponse>

    @GET("d94df912-0d96-42d2-9fb8-036b0565ed03")
    suspend fun getAllComplaints(): NetworkResponse<AllComplaintsResponse>

//    @GET("4eaa4106-f2e8-4ffb-871e-a758d622152a") //Sent Status
    @GET("934f8fcd-7ae7-465b-a017-f0a37e4c7399") // progress
//    @GET("bf0edf89-2c5b-4346-bc4f-0e6f450d9a0d") // resolved with rating
//    @GET("cc9537ae-048b-4ca8-8967-69dc95fc29c0") // resolved without rating
    suspend fun getComplaintStatus(@Query(NetworkConstants.NetworkQueryConstants.ID) id: String): NetworkResponse<ComplaintStatus>

    @POST("191a7744-3474-4b20-9884-5e81a3b791db")
    suspend fun acknowledgeComplaint(@Query(NetworkConstants.NetworkQueryConstants.ID) id: String, @Body comment: UpdateComplaintRequest): NetworkResponse<StatusResponse>

    @POST("191a7744-3474-4b20-9884-5e81a3b791db")
    suspend fun finishComplaint(@Query(NetworkConstants.NetworkQueryConstants.ID) id: String, @Body comment: UpdateComplaintRequest): NetworkResponse<StatusResponse>

    @POST("191a7744-3474-4b20-9884-5e81a3b791db")
    suspend fun withdrawComplaint(@Query(NetworkConstants.NetworkQueryConstants.ID) id: String, @Body comment: UpdateComplaintRequest): NetworkResponse<StatusResponse>

    @POST("191a7744-3474-4b20-9884-5e81a3b791db")
    suspend fun rateComplaintService(@Query(NetworkConstants.NetworkQueryConstants.ID) id: String, @Body rating: RateComplaintServiceRequest): NetworkResponse<StatusResponse>
}