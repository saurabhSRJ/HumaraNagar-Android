package com.humara.nagar.ui

import com.humara.nagar.constants.NetworkConstants
import com.humara.nagar.network.NetworkResponse
import com.humara.nagar.ui.signup.model.*
import retrofit2.http.Body
import retrofit2.http.POST

interface AppConfigService {
    @POST(NetworkConstants.NetworkAPIConstants.CONFIG)
    suspend fun getAppConfig(@Body request: AppConfigRequest): NetworkResponse<AppConfigResponse>

    @POST(NetworkConstants.NetworkAPIConstants.REF_DATA)
    suspend fun getReferenceDetails(@Body request: UserReferenceDataRequest): NetworkResponse<UserReferenceDataResponse>

    @POST(NetworkConstants.NetworkAPIConstants.LOGOUT)
    suspend fun logout(@Body request: LogoutRequest): NetworkResponse<Any>
}