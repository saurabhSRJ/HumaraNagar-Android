package com.humara.nagar.ui.report

import android.content.Context
import com.humara.nagar.network.BaseRepository
import com.humara.nagar.network.retrofit.ApiService
import com.humara.nagar.ui.report.model.ComplaintsRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReportRepository(context: Context) : BaseRepository(context) {

    private val apiService = getRetrofit().create(ApiService::class.java)

    suspend fun postReportComplaint(complaintsRequest: ComplaintsRequest) = withContext(Dispatchers.IO) {
        apiService.postReportComplaint(complaintsRequest)
    }
}