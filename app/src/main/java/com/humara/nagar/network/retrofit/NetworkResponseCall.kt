package com.humara.nagar.network.retrofit

import com.google.gson.Gson
import com.humara.nagar.Logger
import com.humara.nagar.network.ApiError
import com.humara.nagar.network.ErrorResponse
import com.humara.nagar.network.NetworkResponse
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NetworkResponseCall<T>(
    private val proxy: Call<T>
) : Call<NetworkResponse<T>> {
    override fun enqueue(callback: Callback<NetworkResponse<T>>) {
        return proxy.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                val body = response.body()
                val code = response.code()
                if (response.isSuccessful && body != null) {
                    callback.onResponse(
                        this@NetworkResponseCall,
                        Response.success(NetworkResponse.Success(body))
                    )
                } else {
                    callback.onResponse(
                        this@NetworkResponseCall,
                        Response.success(NetworkResponse.Error(ApiError(responseCode = code, message = getErrorMessage(response))))
                    )
                }
            }

            override fun onFailure(call: Call<T>, throwable: Throwable) {
                callback.onResponse(
                    this@NetworkResponseCall,
                    Response.success(NetworkResponse.Exception(throwable))
                )
            }
        })
    }

    private fun getErrorMessage(response: Response<T>): String? {
        val errorString = response.errorBody()?.string()
        var errorMessage: String? = ""
        try {
            if (errorString.isNullOrBlank().not()) {
                errorMessage = Gson().fromJson(errorString, ErrorResponse::class.java).message
            }
        } catch (e: java.lang.Exception) {
            Logger.logException("NetworkResponseCall", e, Logger.LogLevel.ERROR)
        }
        return errorMessage
    }

    override fun execute(): Response<NetworkResponse<T>> = throw NotImplementedError()
    override fun clone(): Call<NetworkResponse<T>> = NetworkResponseCall(proxy.clone())
    override fun request(): Request = proxy.request()
    override fun timeout(): Timeout = proxy.timeout()
    override fun isExecuted(): Boolean = proxy.isExecuted
    override fun isCanceled(): Boolean = proxy.isCanceled
    override fun cancel() = proxy.cancel()
}