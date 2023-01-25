package com.humara.nagar.ui

import android.content.Context
import com.humara.nagar.network.BaseRepository
import com.humara.nagar.network.retrofit.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppConfigRepository(context: Context) : BaseRepository(context) {
    private val apiService = getRetrofit().create(ApiService::class.java)

    suspend fun getAppConfig() = withContext(Dispatchers.IO) { apiService.getUsers() }
}