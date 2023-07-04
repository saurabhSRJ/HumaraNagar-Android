package com.humara.nagar.ui.add_user

import android.app.Application
import com.humara.nagar.network.BaseRepository
import com.humara.nagar.ui.add_user.model.AddUserDetailsRequest
import com.humara.nagar.ui.signup.signup_or_login.model.SendOtpRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddUserRepository(application: Application) : BaseRepository(application) {
    private val apiService = getRetrofit().create(AddUserService::class.java)

    suspend fun sendOtp(request: SendOtpRequest) = withContext(Dispatchers.IO) {
        apiService.sendOtp(request)
    }

    suspend fun createUser(request: AddUserDetailsRequest) = withContext(Dispatchers.IO) {
        apiService.createUser(request)
    }
}