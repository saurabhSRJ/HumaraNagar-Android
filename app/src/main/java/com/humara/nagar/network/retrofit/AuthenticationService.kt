package com.humara.nagar.network.retrofit

import com.humara.nagar.constants.NetworkConstants
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthenticationService {
    @POST(NetworkConstants.NetworkAPIConstants.TOKEN)
    fun getAccessTokenFromRefreshToken(@Body request: TokenRefreshRequest): Call<TokenRefreshResponse>
}