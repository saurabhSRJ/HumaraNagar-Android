package com.humara.nagar.ui.report

import android.app.Application
import com.humara.nagar.network.BaseRepository
import com.humara.nagar.ui.report.model.PostComplaintRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReportRepository(application: Application) : BaseRepository(application) {
    private val apiService = getRetrofit().create(ReportService::class.java)

    suspend fun postComplaint(request: PostComplaintRequest) = withContext(Dispatchers.IO) {
        apiService.postComplaint(request)
    }
}