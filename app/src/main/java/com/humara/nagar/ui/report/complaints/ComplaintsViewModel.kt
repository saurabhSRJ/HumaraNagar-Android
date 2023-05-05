package com.humara.nagar.ui.report.complaints

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.network.onError
import com.humara.nagar.network.onSuccess
import com.humara.nagar.ui.report.model.AllComplaintsResponse
import kotlinx.coroutines.launch

class ComplaintsViewModel(application: Application) : BaseViewModel(application) {
    private val repository = ComplaintsRepository(application)
    private val _allComplaintLiveData: MutableLiveData<AllComplaintsResponse> = MutableLiveData()
    val allComplaintLiveData: LiveData<AllComplaintsResponse> = _allComplaintLiveData

    init {
        getAllComplaints()
    }

    fun getAllComplaints() = viewModelScope.launch {
        val response = processCoroutine({ repository.getAllComplaints() })
        response.onSuccess {
            _allComplaintLiveData.postValue(it)
        }.onError {
            errorLiveData.postValue(it)
        }
    }
}