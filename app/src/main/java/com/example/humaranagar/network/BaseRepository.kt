package com.example.humaranagar.network

import android.app.Application
import android.content.Context
import com.example.humaranagar.network.retrofit.ApiService
import com.example.humaranagar.network.retrofit.RetrofitService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Base repository which can be used to get the singleton Retrofit instance
 */
open class BaseRepository(val context: Context) {
    protected fun getRetrofit() = RetrofitService.getInstance(context).retrofitInstance

    private val apiService = getRetrofit().create(ApiService::class.java)

    suspend fun getUsers() = withContext(Dispatchers.IO) {
        apiService.getUsers()
    }
}