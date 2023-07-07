package com.humara.nagar.ui.report

import com.humara.nagar.constants.NetworkConstants
import com.humara.nagar.network.NetworkResponse
import com.humara.nagar.ui.report.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ReportService {
    @Multipart
    @POST(NetworkConstants.NetworkAPIConstants.COMPLAINT)
    suspend fun postComplaint(@PartMap partMap: MutableMap<String, RequestBody>, @Part image: ArrayList<MultipartBody.Part>): NetworkResponse<PostComplaintResponse>

    @GET(NetworkConstants.NetworkAPIConstants.COMPLAINT)
    suspend fun getComplaints(
        @Query(NetworkConstants.NetworkQueryConstants.PAGE) page: Int,
        @Query(NetworkConstants.NetworkQueryConstants.LIMIT) limit: Int,
        @Query(NetworkConstants.NetworkQueryConstants.WARD_ID) wardId: Int,
        @Query(NetworkConstants.NetworkQueryConstants.FILTER_ID) filterId: Int
    ): NetworkResponse<AllComplaintsResponse>

    @GET(NetworkConstants.NetworkAPIConstants.COMPLAINT_DETAILS)
    suspend fun getComplaintStatus(@Path(NetworkConstants.NetworkQueryConstants.ID) id: String): NetworkResponse<ComplaintStatus>

    @POST(NetworkConstants.NetworkAPIConstants.ACKNOWLEDGE_COMPLAINT)
    suspend fun acknowledgeComplaint(@Path(NetworkConstants.NetworkQueryConstants.ID) id: String, @Body comment: UpdateComplaintRequest): NetworkResponse<StatusResponse>

    @POST(NetworkConstants.NetworkAPIConstants.FINISH_COMPLAINT)
    suspend fun finishComplaint(@Path(NetworkConstants.NetworkQueryConstants.ID) id: String, @Body comment: UpdateComplaintRequest): NetworkResponse<StatusResponse>

    @POST(NetworkConstants.NetworkAPIConstants.WITHDRAW_COMPLAINT)
    suspend fun withdrawComplaint(@Path(NetworkConstants.NetworkQueryConstants.ID) id: String, @Body comment: UpdateComplaintRequest): NetworkResponse<StatusResponse>

    @POST(NetworkConstants.NetworkAPIConstants.RATE_COMPLAINT)
    suspend fun rateComplaintService(@Path(NetworkConstants.NetworkQueryConstants.ID) id: String, @Body rating: RateComplaintServiceRequest): NetworkResponse<StatusResponse>
}