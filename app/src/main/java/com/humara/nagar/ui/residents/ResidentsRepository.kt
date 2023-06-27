package com.humara.nagar.ui.residents

import android.app.Application
import com.humara.nagar.network.BaseRepository
import com.humara.nagar.ui.residents.model.EmptyRequestBody
import com.humara.nagar.ui.residents.model.GetResidentsRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ResidentsRepository(application: Application) : BaseRepository(application) {
    private val apiService = getRetrofit().create(ResidentsService::class.java)

    suspend fun getAllResidents(page: Int, limit: Int, request: EmptyRequestBody) = withContext(Dispatchers.IO) {
        apiService.getAllResidents(page, limit, request)
    }

    suspend fun searchResidentList(searchText: String, request: EmptyRequestBody) = withContext(Dispatchers.IO) {
        apiService.searchResidentList(searchText, request)
    }
}