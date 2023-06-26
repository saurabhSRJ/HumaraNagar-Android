package com.humara.nagar.ui.residents

import android.app.Application
import com.humara.nagar.network.BaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ResidentsRepository(application: Application) : BaseRepository(application) {
    private val apiService = getRetrofit().create(ResidentsService::class.java)

    suspend fun getAllResidents(page: Int, limit: Int, searchText: String?) = withContext(Dispatchers.IO) {
        apiService.getAllResidents(page, limit, searchText)
    }
}