package com.humara.nagar.ui.signup

import com.humara.nagar.constants.NetworkConstants
import com.humara.nagar.network.NetworkResponse
import com.humara.nagar.ui.signup.model.AppConfigResponse
import com.humara.nagar.ui.signup.otp_verification.model.LoginRequest
import com.humara.nagar.ui.signup.otp_verification.model.LoginResponse
import com.humara.nagar.ui.signup.profile_creation.model.ProfileCreationRequest
import com.humara.nagar.ui.signup.signup_or_login.model.SendOtpRequest
import com.humara.nagar.ui.signup.signup_or_login.model.SendOtpResponse
import com.humara.nagar.ui.signup.signup_or_login.model.SignupResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface OnBoardingService {
    @POST(NetworkConstants.NetworkAPIConstants.OTP)
    suspend fun sendOtp(@Body request: SendOtpRequest): NetworkResponse<SendOtpResponse>

    @POST(NetworkConstants.NetworkAPIConstants.LOGIN)
    suspend fun verifyOtpAndLogin(@Body request: LoginRequest): NetworkResponse<LoginResponse>

    @POST(NetworkConstants.NetworkAPIConstants.SIGNUP)
    suspend fun signup(@Body request: ProfileCreationRequest): NetworkResponse<SignupResponse>

    @Multipart
    @PUT(NetworkConstants.NetworkAPIConstants.UPDATE_IMAGE)
    suspend fun updateProfileImage(@Part image: MultipartBody.Part): NetworkResponse<AppConfigResponse>
}