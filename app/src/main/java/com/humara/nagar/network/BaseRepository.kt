package com.humara.nagar.network

import android.content.Context
import com.humara.nagar.network.retrofit.RetrofitService

/**
 * Base repository which can be used to get the singleton Retrofit instance
 */
open class BaseRepository(val context: Context) {
    protected fun getRetrofit() = RetrofitService.getInstance(context).retrofitInstance
}