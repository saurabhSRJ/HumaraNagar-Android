package com.humara.nagar.ui.report.complaints

import ApiService
import android.content.Context
import com.humara.nagar.network.BaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class AllComplaintsRepository(context: Context) : BaseRepository(context) {

    private val apiService = getRetrofit().create(ApiService::class.java)

    suspend fun getAllComplaints() = withContext(Dispatchers.IO) {
        apiService.getAllComplaints()
    }
}