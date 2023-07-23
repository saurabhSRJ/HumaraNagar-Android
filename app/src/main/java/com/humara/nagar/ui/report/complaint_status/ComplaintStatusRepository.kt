package com.humara.nagar.ui.report.complaint_status

import android.app.Application
import com.humara.nagar.network.BaseRepository
import com.humara.nagar.ui.report.ReportService
import com.humara.nagar.ui.report.model.RateComplaintServiceRequest
import com.humara.nagar.ui.report.model.UpdateComplaintRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ComplaintStatusRepository(application: Application) : BaseRepository(application) {
    private val apiService = getRetrofit().create(ReportService::class.java)

    suspend fun getComplaintStatus(id: String) = withContext(Dispatchers.IO) {
        apiService.getComplaintStatus(id)
    }

    suspend fun acknowledgeComplaint(id: String, request: UpdateComplaintRequest) = withContext(Dispatchers.IO) {
        apiService.acknowledgeComplaint(id, request)
    }

    suspend fun finishComplaint(id: String, comment: String) = withContext(Dispatchers.IO) {
        apiService.finishComplaint(id, UpdateComplaintRequest(comment))
    }

    suspend fun withdrawComplaint(id: String, comment: String) = withContext(Dispatchers.IO) {
        apiService.withdrawComplaint(id, UpdateComplaintRequest(comment))
    }

    suspend fun rateComplaintService(id: String, rating: Int, comment: String?) = withContext(Dispatchers.IO) {
        apiService.rateComplaintService(id, RateComplaintServiceRequest(rating, comment))
    }
}