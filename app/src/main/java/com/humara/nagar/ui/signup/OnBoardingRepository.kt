package com.humara.nagar.ui.signup

import android.app.Application
import android.net.Uri
import com.humara.nagar.constants.NetworkConstants
import com.humara.nagar.network.BaseRepository
import com.humara.nagar.ui.signup.otp_verification.model.LoginRequest
import com.humara.nagar.ui.signup.profile_creation.model.ProfileCreationRequest
import com.humara.nagar.ui.signup.signup_or_login.model.SendOtpRequest
import com.humara.nagar.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OnBoardingRepository(application: Application) : BaseRepository(application) {
    private val apiService = getRetrofit().create(OnBoardingService::class.java)

    suspend fun sendOtp(request: SendOtpRequest) = withContext(Dispatchers.IO) {
        apiService.sendOtp(request)
    }

    suspend fun verifyOtpAndLogin(request: LoginRequest) = withContext(Dispatchers.IO) {
        apiService.verifyOtpAndLogin(request)
    }

    suspend fun signup(request: ProfileCreationRequest) = withContext(Dispatchers.IO) {
        apiService.signup(request)
    }

    suspend fun updateProfileImage(imageUri: Uri) = withContext(Dispatchers.IO) {
        apiService.updateProfileImage(NetworkUtils.createImageMultipart(imageUri, NetworkConstants.NetworkFormDataConstants.IMAGE))
    }
}
