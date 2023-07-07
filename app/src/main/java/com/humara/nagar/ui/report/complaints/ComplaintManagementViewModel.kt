package com.humara.nagar.ui.report.complaints

import android.app.Application
import androidx.lifecycle.LiveData
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.utils.SingleLiveEvent

class ComplaintManagementViewModel(application: Application): BaseViewModel(application) {
    private val _complaintsListReloadLiveData: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }
    val complaintsListReloadLiveData: LiveData<Boolean> = _complaintsListReloadLiveData

    fun setReloadComplaintsList() {
        _complaintsListReloadLiveData.value = true
    }
}