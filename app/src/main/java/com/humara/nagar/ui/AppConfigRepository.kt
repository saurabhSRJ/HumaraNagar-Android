package com.humara.nagar.ui

import android.content.Context
import com.humara.nagar.network.BaseRepository
import com.humara.nagar.network.retrofit.ApiService
import com.humara.nagar.ui.signup.model.UserConfigRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppConfigRepository(context: Context) : BaseRepository(context) {
    private val apiService = getRetrofit().create(ApiService::class.java)

    suspend fun getAppConfig(request: UserConfigRequest) = withContext(Dispatchers.IO) {
        apiService.getUserConfig(request)
    }
}