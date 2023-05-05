package com.humara.nagar.ui

import android.app.Application
import android.content.Context
import com.humara.nagar.network.BaseRepository
import com.humara.nagar.ui.signup.model.AppConfigRequest
import com.humara.nagar.ui.signup.model.LogoutRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppConfigRepository(context: Application) : BaseRepository(context) {
    private val apiService = getRetrofit().create(AppConfigService::class.java)

    suspend fun getAppConfig(request: AppConfigRequest) = withContext(Dispatchers.IO) {
        apiService.getAppConfig(request)
    }

    suspend fun logout(request: LogoutRequest) = withContext(Dispatchers.IO) {
        apiService.logout(request)
    }
}