package com.humara.nagar.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.network.onError
import com.humara.nagar.network.onSuccess
import com.humara.nagar.ui.signup.model.UserConfigRequest
import com.humara.nagar.ui.signup.model.UserConfigResponse
import kotlinx.coroutines.launch

class AppConfigViewModel(application: Application) : BaseViewModel(application) {
    private val repository = AppConfigRepository(application)
    private val _appConfigLiveData: MutableLiveData<UserConfigResponse> by lazy { MutableLiveData() }
    val appConfigLiveData: LiveData<UserConfigResponse> = _appConfigLiveData

    fun getAppConfig() = viewModelScope.launch {
        val response = processCoroutine({ repository.getAppConfig(UserConfigRequest(getUserPreference().mobileNumber)) })
        response.onSuccess {
            _appConfigLiveData.postValue(it)
        }.onError {
            errorLiveData.postValue(it)
        }
    }
}