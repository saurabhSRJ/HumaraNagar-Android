package com.humara.nagar.ui.residents

import ApiService
import android.content.Context
import com.humara.nagar.network.BaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class ResidentsRepository(context: Context) : BaseRepository(context) {

    private val apiService = getRetrofit().create(ApiService::class.java)

    suspend fun requestAllResidentsList() = withContext(Dispatchers.IO) {
        apiService.requestAllResidentsList()
    }

    suspend fun requestAllFilters() = withContext(Dispatchers.IO) {
        apiService.requestAllFilters()
    }
}