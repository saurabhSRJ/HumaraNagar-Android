package com.humara.nagar.ui.user_profile

import android.app.Application
import com.humara.nagar.network.BaseRepository
import com.humara.nagar.ui.user_profile.model.UpdateUserProfileRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserProfileRepository(application: Application): BaseRepository(application) {
    private val apiService = getRetrofit().create(UserProfileService::class.java)

    suspend fun getUserProfile(id: Long) = withContext(Dispatchers.IO) {
        apiService.getUserProfile(id)
    }

    suspend fun updateUserProfile(id: Long, request: UpdateUserProfileRequest) = withContext(Dispatchers.IO) {
        apiService.updateUserProfile(id, request)
    }
}