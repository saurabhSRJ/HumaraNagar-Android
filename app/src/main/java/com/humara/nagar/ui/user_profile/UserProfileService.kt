package com.humara.nagar.ui.user_profile

import com.humara.nagar.constants.NetworkConstants
import com.humara.nagar.network.NetworkResponse
import com.humara.nagar.ui.user_profile.model.UpdateUserProfileRequest
import com.humara.nagar.ui.user_profile.model.UserProfileResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserProfileService {
    @GET(NetworkConstants.NetworkAPIConstants.USER_PROFILE)
    suspend fun getUserProfile(@Path(NetworkConstants.NetworkQueryConstants.ID) id: Long): NetworkResponse<UserProfileResponse>

    @PUT(NetworkConstants.NetworkAPIConstants.USER_PROFILE)
    suspend fun updateUserProfile(@Path(NetworkConstants.NetworkQueryConstants.ID) id: Long, @Body requset: UpdateUserProfileRequest): NetworkResponse<UserProfileResponse>
}