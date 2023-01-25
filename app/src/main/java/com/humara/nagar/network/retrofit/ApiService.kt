package com.humara.nagar.network.retrofit

import com.humara.nagar.network.NetworkResponse
import retrofit2.http.GET

interface ApiService {
    @GET("users")
    suspend fun getUsers(): NetworkResponse<List<User>>
}