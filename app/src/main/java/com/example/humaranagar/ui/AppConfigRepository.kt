package com.example.humaranagar.ui

import android.content.Context
import com.example.humaranagar.network.BaseRepository
import com.example.humaranagar.network.retrofit.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppConfigRepository(context: Context) : BaseRepository(context) {
    private val apiService = getRetrofit().create(ApiService::class.java)

    suspend fun getAppConfig() = withContext(Dispatchers.IO) { apiService.getUsers() }
}