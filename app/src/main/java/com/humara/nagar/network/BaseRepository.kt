package com.humara.nagar.network

import android.app.Application
import com.humara.nagar.network.retrofit.RetrofitService

/**
 * Base repository which can be used to get the singleton Retrofit instance
 */
open class BaseRepository(val application: Application) {
    protected fun getRetrofit() = RetrofitService.getInstance(application).retrofitInstance
}