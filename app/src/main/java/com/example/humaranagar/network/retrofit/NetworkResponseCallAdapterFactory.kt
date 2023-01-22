package com.example.humaranagar.network.retrofit

import com.example.humaranagar.network.NetworkResponse
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class NetworkResponseCallAdapterFactory: CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java) {
            return null
        }

        val callType = getParameterUpperBound(0, returnType as ParameterizedType)
        if (getRawType(callType) !=  NetworkResponse::class.java) {
            return null
        }

        val responseType = getParameterUpperBound(0, callType as ParameterizedType)
        return NetworkResponseAdapter(responseType)
    }
}