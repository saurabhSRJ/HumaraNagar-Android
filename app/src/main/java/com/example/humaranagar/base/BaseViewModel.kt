package com.example.humaranagar.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.humaranagar.NagarApp
import com.example.humaranagar.network.*
import com.example.humaranagar.shared_pref.AppPreference
import com.example.humaranagar.shared_pref.UserPreference
import com.example.humaranagar.utils.SingleLiveEvent
import kotlinx.coroutines.delay
import java.net.HttpURLConnection

/**
 * Base ViewModel for other view models. Provides some common functionality for error handling and api response processing
 * NOTE: Always call observerErrorAndException() function from the base activity/fragment for error handling to work
 */
open class BaseViewModel(application: Application) :
    AndroidViewModel(application) {
    /**
     * LiveData to show progress in activity/fragment
     */
    val progressLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }

    /**
     * Common LiveData to handle error in activity/fragment. If required, you can use your own live data to handle error scenarios also.
     */
    val errorLiveData: SingleLiveEvent<ApiError> by lazy { SingleLiveEvent() }

    /**
     * LiveData to handle exceptions like no internet connection etc
     */
    val exceptionLiveData: SingleLiveEvent<Throwable> by lazy { SingleLiveEvent() }

    /**
     *  Method to help processing of API response. If further processing of response is not needed you can pass the LiveData to directly post value
     *  @param call: Network call to be executed
     *  @param updateProgress: Boolean value indicating if you want to track the progress using progressLiveData
     *  @return
     */
    suspend fun <T : Any> processCoroutine(
        call: suspend () -> NetworkResponse<T>,
        updateProgress: Boolean = true
    ): NetworkResponse<T> {
        if (updateProgress) progressLiveData.postValue(true)
        val response = call.invoke()
        response.onError {
            if (it.responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                getApplication<NagarApp>().logout()
            }
        }.onException {
            exceptionLiveData.postValue(it)
        }
        progressLiveData.postValue(false)
        return response
    }

    /**
     *  Method to help processing of API response. If further processing of response is not needed you can pass the LiveData to directly post value
     *  @param call: Network call to be executed
     *  @param successLiveData: LiveData if API call is success
     *  @param errorLiveData: LiveData if API throws error. Default value is the base errorLiveData
     *  @param updateProgress: Boolean value indicating if you want to track the progress using progressLiveData
     */
    suspend fun <T : Any> postCoroutineResponse(
        call: suspend () -> NetworkResponse<T>,
        successLiveData: MutableLiveData<T>,
        errorLiveData: MutableLiveData<ApiError>? = this.errorLiveData,
        updateProgress: Boolean = true
    ) {
        val response = processCoroutine(call, updateProgress)
        response.onSuccess {
            successLiveData.postValue(it)
        }.onError {
            errorLiveData?.postValue(it)
        }.onException {
            exceptionLiveData.postValue(it)
        }
    }

    /**
     * Return User preference data(i.e user profile) being set and used throughout the app.
     * @return [UserPreference]
     */
    protected fun getUserPreference(): UserPreference =
        (getApplication() as NagarApp).userSharedPreference

    /**
     * Return App preference being set and used throughout the app.
     * @return [AppPreference]
     */
    protected fun getAppPreference(): AppPreference =
        (getApplication() as NagarApp).appSharedPreference
}