package com.humara.nagar.ui.add_user

import com.humara.nagar.constants.NetworkConstants
import com.humara.nagar.network.NetworkResponse
import com.humara.nagar.ui.add_user.model.AddUserDetailsRequest
import com.humara.nagar.ui.add_user.model.UserOtpResponse
import com.humara.nagar.ui.signup.signup_or_login.model.SendOtpRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AddUserService {
    @POST(NetworkConstants.NetworkAPIConstants.USER_OTP)
    suspend fun sendOtp(@Body request: SendOtpRequest): NetworkResponse<UserOtpResponse>

    @POST(NetworkConstants.NetworkAPIConstants.USER_SIGNUP)
    suspend fun createUser(@Body request: AddUserDetailsRequest): NetworkResponse<Any>
}