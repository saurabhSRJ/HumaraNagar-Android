package com.humara.nagar.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.network.onError
import com.humara.nagar.network.onSuccess
import kotlinx.coroutines.launch

class AppConfigViewModel(application: Application) : BaseViewModel(application) {
    private val repository = AppConfigRepository(application)
    private val _appConfigLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val appConfigLiveData: LiveData<Boolean> = _appConfigLiveData

    fun getAppConfig() = viewModelScope.launch {
        //TODO: Add app config API
        val response = processCoroutine({ repository.getUsers() })
        response.onSuccess {
            _appConfigLiveData.postValue(true)
        }.onError {
            errorLiveData.postValue(it)
        }
    }
}