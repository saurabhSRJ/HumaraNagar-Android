package com.humara.nagar.ui.report.complaints

import android.app.Application
import com.humara.nagar.network.BaseRepository
import com.humara.nagar.ui.report.ReportService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ComplaintsRepository(application: Application) : BaseRepository(application) {
    private val apiService = getRetrofit().create(ReportService::class.java)

    suspend fun getComplaints(page: Int, limit: Int, wardId: Int, filterId: Int) = withContext(Dispatchers.IO) {
        apiService.getComplaints(page, limit, wardId, filterId)
    }
}