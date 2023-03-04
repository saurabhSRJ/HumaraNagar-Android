package com.humara.nagar.ui.report.complaint_status

import android.content.Context
import com.humara.nagar.network.BaseRepository
import com.humara.nagar.network.retrofit.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ComplaintStatusRepository(context: Context) : BaseRepository(context) {

    private val apiService = getRetrofit().create(ApiService::class.java)

    suspend fun getComplaintStatus() = withContext(Dispatchers.IO) {
        apiService.getComplaintStatus()
    }

    suspend fun postAcknowledge(id: String, comment: String) = withContext(Dispatchers.IO) {
        apiService.requestAcknowledge(id, comment)
    }

    suspend fun postFinish(id: String, comment: String) = withContext(Dispatchers.IO) {
        apiService.requestFinish(id, comment)
    }

    suspend fun postWithdraw(id: String, comment: String) = withContext(Dispatchers.IO) {
        apiService.requestWithdraw(id, comment)
    }

    suspend fun postRating(id: String, rating: Int) = withContext(Dispatchers.IO) {
        apiService.requestRating(id, rating)
    }
}