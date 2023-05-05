package com.humara.nagar.ui.report.complaints

import android.content.Context
import com.humara.nagar.network.BaseRepository
import com.humara.nagar.ui.report.ReportService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ComplaintsRepository(context: Context) : BaseRepository(context) {
    private val apiService = getRetrofit().create(ReportService::class.java)

    suspend fun getAllComplaints() = withContext(Dispatchers.IO) {
        apiService.getAllComplaints()
    }
}