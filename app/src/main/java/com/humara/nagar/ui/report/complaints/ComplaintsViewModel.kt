package com.humara.nagar.ui.report.complaints

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.network.onError
import com.humara.nagar.network.onSuccess
import com.humara.nagar.ui.report.model.AllComplaintsResponse
import kotlinx.coroutines.launch

class ComplaintsViewModel(
    application: Application
) : BaseViewModel(application) {

    private val repository = AllComplaintsRepository(application)
    val allComplaintLiveData: MutableLiveData<AllComplaintsResponse> = MutableLiveData()

    fun getAllComplaints() = viewModelScope.launch {
        val response = processCoroutine({ repository.getAllComplaints() })
        response.onSuccess {
            allComplaintLiveData.value = it
        }.onError {
            errorLiveData.postValue(it)
        }
    }
}