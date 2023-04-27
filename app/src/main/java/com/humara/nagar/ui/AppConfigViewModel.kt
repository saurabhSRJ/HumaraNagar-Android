package com.humara.nagar.ui

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.network.onError
import com.humara.nagar.network.onSuccess
import com.humara.nagar.ui.signup.model.AppConfigRequest
import com.humara.nagar.ui.signup.model.AppConfigResponse
import com.humara.nagar.ui.signup.model.LogoutRequest
import kotlinx.coroutines.launch

class AppConfigViewModel(application: Application) : BaseViewModel(application) {
    private val repository = AppConfigRepository(application)
    private val _appConfigLiveData: MutableLiveData<AppConfigResponse> by lazy { MutableLiveData() }
    val appConfigLiveData: LiveData<AppConfigResponse> = _appConfigLiveData

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
        repository.logout(LogoutRequest(getUserPreference().mobileNumber, getUserPreference().refreshToken))
    }
}