package com.humara.nagar.fcm

import android.app.Application
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.humara.nagar.Logger
import com.humara.nagar.network.BaseRepository
import com.humara.nagar.network.NetworkResponse
import com.humara.nagar.network.retrofit.ApiService
import com.humara.nagar.ui.report.model.StatusResponse
import com.humara.nagar.utils.getUserSharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FcmTokenUploadRepository(application: Application) : BaseRepository(application) {
    private val apiService = getRetrofit().create(ApiService::class.java)
    /**
     * Get the push token using Firebase SDK
     */
    suspend fun fetchFcmToken(): Task<String> = withContext(Dispatchers.IO) {
        return@withContext FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Logger.debugLog("Fetching FCM registration token failed")
                return@OnCompleteListener
            }
            // Get new FCM registration token
            return@OnCompleteListener
        })
    }

    suspend fun updateFcmTokenToServer(newToken: String): NetworkResponse<StatusResponse> {
        return apiService.updateFcmTokenToServer(FcmTokenRequest(application.getUserSharedPreferences().userProfile?.userId ?: 0, newToken))
    }
}