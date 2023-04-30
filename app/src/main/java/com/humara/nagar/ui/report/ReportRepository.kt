package com.humara.nagar.ui.report

import android.content.Context
import com.humara.nagar.network.BaseRepository
import com.humara.nagar.ui.report.model.PostComplaintRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReportRepository(context: Context) : BaseRepository(context) {
    private val apiService = getRetrofit().create(ReportsService::class.java)

    suspend fun postComplaint(request: PostComplaintRequest) = withContext(Dispatchers.IO) {
        apiService.postComplaint(request)
    }
}