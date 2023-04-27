package com.humara.nagar.ui.signup

import com.humara.nagar.network.NetworkResponse
import com.humara.nagar.ui.signup.otp_verification.model.LoginRequest
import com.humara.nagar.ui.signup.otp_verification.model.LoginResponse
import com.humara.nagar.ui.signup.profile_creation.model.ProfileCreationRequest
import com.humara.nagar.ui.signup.signup_or_login.model.SendOtpRequest
import com.humara.nagar.ui.signup.signup_or_login.model.SendOtpResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface OnBoardingService {
    //    @POST(NetworkConstants.NetworkAPIConstants.OTP)
    // approval pending @POST("1290d2ac-e5d3-4be8-acc7-c48e2c717b3d")
    @POST("9f2505bb-13c9-4c6e-8cf5-2422575da66d")
    suspend fun sendOtp(@Body request: SendOtpRequest): NetworkResponse<SendOtpResponse>

    //    @POST(NetworkConstants.NetworkAPIConstants.LOGIN)
    @POST("d6eb47a5-1f0c-43b1-abc2-dac497262fc1")
    suspend fun verifyOtpAndLogin(@Body request: LoginRequest): NetworkResponse<LoginResponse>

    //    @POST(NetworkConstants.NetworkAPIConstants.SIGNUP)
    @POST("d314af8e-f0f5-47fa-9ce3-7f90aaa03f66")
    suspend fun signup(@Body request: ProfileCreationRequest): NetworkResponse<Any>
}