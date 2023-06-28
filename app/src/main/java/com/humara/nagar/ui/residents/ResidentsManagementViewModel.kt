package com.humara.nagar.ui.residents

import android.app.Application
import androidx.lifecycle.LiveData
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.utils.SingleLiveEvent

class ResidentsManagementViewModel(application: Application): BaseViewModel(application) {
    private val _userAdditionSuccessLiveData: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }
    val userAdditionSuccessLiveData: LiveData<Boolean> = _userAdditionSuccessLiveData

    fun setUserAdditionSuccess() {
        _userAdditionSuccessLiveData.value = true
    }
}