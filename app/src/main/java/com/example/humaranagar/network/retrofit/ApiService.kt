package com.example.humaranagar.network.retrofit

import com.example.humaranagar.network.NetworkResponse
import retrofit2.http.GET

interface ApiService {
    @GET("users")
    suspend fun getUsers(): NetworkResponse<List<User>>
}