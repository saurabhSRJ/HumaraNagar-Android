package com.humara.nagar.ui

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.humara.nagar.Logger
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.fcm.FcmTokenUploadRepository
import com.humara.nagar.network.onError
import com.humara.nagar.network.onSuccess
import com.humara.nagar.ui.signup.model.AppConfigRequest
import com.humara.nagar.ui.signup.model.AppConfigResponse
import com.humara.nagar.ui.signup.model.LogoutRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class AppConfigViewModel(application: Application) : BaseViewModel(application) {
    private val repository = AppConfigRepository(application)
    private val fcmRepository = FcmTokenUploadRepository(application)
    private val _appConfigLiveData: MutableLiveData<AppConfigResponse> by lazy { MutableLiveData() }
    val appConfigLiveData: LiveData<AppConfigResponse> = _appConfigLiveData
    private val _logoutLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val logoutLiveData: LiveData<Boolean> = _logoutLiveData

    fun getAppConfig() = viewModelScope.launch {
        val response = processCoroutine({ repository.getAppConfig(AppConfigRequest(getUserPreference().mobileNumber)) })
        response.onSuccess {
            getUserPreference().role = it.role
            _appConfigLiveData.postValue(it)
        }.onError {
            errorLiveData.postValue(it)
        }
    }

    fun logout() = viewModelScope.launch {
        val response = processCoroutine({ repository.logout(LogoutRequest(getUserPreference().mobileNumber, getUserPreference().refreshToken)) })
        //Logging out user in any case for now.
        response.onSuccess {
            _logoutLiveData.postValue(true)
        }.onError {
            _logoutLiveData.postValue(true)
        }
    }

    fun fetchFcmTokenAndUpdateToServerIfRequired() = viewModelScope.launch {
        val fetchTokenTask = async { fcmRepository.fetchFcmToken() }
        val storedToken = getUserPreference().fcmToken
        val isTokenUpdated = getUserPreference().fcmTokenUpdated
        val newToken = fetchTokenTask.await().result
        Logger.debugLog("fcm toke: $newToken")
        if (isTokenUpdated && storedToken != newToken) {
            val response = processCoroutine({ fcmRepository.updateFcmTokenToServer(newToken) })
            response.onSuccess {
                getUserPreference().fcmToken = newToken
                getUserPreference().fcmTokenUpdated = false
            }
        }
    }
}