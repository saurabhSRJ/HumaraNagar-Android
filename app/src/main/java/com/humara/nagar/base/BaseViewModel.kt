package com.humara.nagar.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.humara.nagar.NagarApp
import com.humara.nagar.network.ApiError
import com.humara.nagar.network.NetworkResponse
import com.humara.nagar.network.onException
import com.humara.nagar.shared_pref.AppPreference
import com.humara.nagar.shared_pref.UserPreference
import com.humara.nagar.utils.SingleLiveEvent

/**
 * Base ViewModel for other view models. Provides some common functionality for error handling and api response processing
 * NOTE: Always call observerErrorAndException() function from the base activity/fragment for error handling to work
 */
open class BaseViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * LiveData to show progress in activity/fragment
     */
    val progressLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }

    /**
     * Common LiveData to handle error in activity/fragment. If required, you can use your own live data to handle error scenarios also.
     */
    val errorLiveData: SingleLiveEvent<ApiError> by lazy { SingleLiveEvent() }

    /**
     * LiveData to handle exceptions like no internet connection, unauthorized exception etc
     */
    val exceptionLiveData: SingleLiveEvent<Throwable> by lazy { SingleLiveEvent() }

    /**
     *  Method to help processing of API response. If further processing of response is not needed you can pass the LiveData to directly post value
     *  @param call: Network call to be executed
     *  @param updateProgress: Boolean value indicating if you want to track the progress using progressLiveData
     *  @return
     */
    suspend fun <T> processCoroutine(
        call: suspend () -> NetworkResponse<T>,
        updateProgress: Boolean = true,
        progressLiveData: MutableLiveData<Boolean> = this.progressLiveData
    ): NetworkResponse<T> {
        if (updateProgress) progressLiveData.postValue(true)
        val response = call.invoke()
        response.onException {
            exceptionLiveData.postValue(it)
        }
        progressLiveData.postValue(false)
        return response
    }

    /**
     * Return User preference data(i.e user profile) being set and used throughout the app.
     * @return [UserPreference]
     */
    protected fun getUserPreference(): UserPreference = getApplication<NagarApp>().userSharedPreference

    /**
     * Return App preference being set and used throughout the app.
     * @return [AppPreference]
     */
    protected fun getAppPreference(): AppPreference = getApplication<NagarApp>().appSharedPreference
}