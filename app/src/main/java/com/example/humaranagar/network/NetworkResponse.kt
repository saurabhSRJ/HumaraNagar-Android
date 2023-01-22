package com.example.humaranagar.network

/**
 * Model for the API response. We get three callbacks {onSuccess, onError, onException} depending upon the response
 */
sealed class NetworkResponse<T : Any> {
    class Success<T : Any>(val data: T) : NetworkResponse<T>()
    class Error<T : Any>(val error: ApiError) : NetworkResponse<T>()
    class Exception<T : Any>(val throwable: Throwable) : NetworkResponse<T>()
}

suspend fun <T : Any> NetworkResponse<T>.onSuccess(
    executable: suspend (T) -> Unit
): NetworkResponse<T> = apply {
    if (this is NetworkResponse.Success<T>) {
        executable(data)
    }
}

suspend fun <T: Any> NetworkResponse<T>.onError(
    executable: suspend (error: ApiError) -> Unit
): NetworkResponse<T> = apply {
    if (this is NetworkResponse.Error<T>) {
        executable(error)
    }
}

suspend fun <T: Any> NetworkResponse<T>.onException(
    executable: suspend (e: Throwable) -> Unit
): NetworkResponse<T> = apply {
    if (this is NetworkResponse.Exception<T>) {
        executable(throwable)
    }
}