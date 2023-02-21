package com.humara.nagar.ui.report.complaint_status

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.network.ApiError
import com.humara.nagar.network.onError
import com.humara.nagar.network.onSuccess
import com.humara.nagar.ui.report.model.ComplaintStatus
import com.humara.nagar.ui.report.model.StatusResponse
import com.humara.nagar.utils.SingleLiveEvent
import kotlinx.coroutines.launch


class ComplaintStatusViewModel(
    application: Application
) : BaseViewModel(application) {

    private val repository = ComplaintStatusRepository(application)
    val complaintStatusLiveData: MutableLiveData<ComplaintStatus> = MutableLiveData()
    val acknowledgementLiveData: MutableLiveData<StatusResponse> = MutableLiveData()
    val finishLiveData: MutableLiveData<StatusResponse> = MutableLiveData()
    val withdrawLiveData: MutableLiveData<StatusResponse> = MutableLiveData()
    val ratingLiveData: MutableLiveData<StatusResponse> = MutableLiveData()
    val complaintStatsErrorLiveData: MutableLiveData<ApiError> by lazy { SingleLiveEvent() }
    val postAcknowledgeErrorLiveData: MutableLiveData<ApiError> by lazy { SingleLiveEvent() }
    val postFinishErrorLiveData: MutableLiveData<ApiError> by lazy { SingleLiveEvent() }
    val postWithdrawErrorLiveData: MutableLiveData<ApiError> by lazy { SingleLiveEvent() }
    val postRatingErrorLiveData: MutableLiveData<ApiError> by lazy { SingleLiveEvent() }

    fun getComplaintStatus() = viewModelScope.launch {
        val response = processCoroutine({ repository.getComplaintStatus() })
        response.onSuccess {
            complaintStatusLiveData.postValue(it)
        }.onError {
            complaintStatsErrorLiveData.postValue(it)
        }
    }

    fun postAcknowledgementRequest(id: String, comment: String) = viewModelScope.launch {
        val response = processCoroutine({ repository.postAcknowledge(id, comment)})
        response.onSuccess {
            acknowledgementLiveData.postValue(it)
        }.onError {
            postAcknowledgeErrorLiveData.postValue(it)
        }
    }
    fun postFinishRequest(id: String, comment: String) = viewModelScope.launch {
        val response = processCoroutine({ repository.postFinish(id, comment)})
        response.onSuccess {
            finishLiveData.postValue(it)
        }.onError {
            postFinishErrorLiveData.postValue(it)
        }
    }
    fun postWithdrawRequest(id: String, comment: String) = viewModelScope.launch {
        val response = processCoroutine({ repository.postWithdraw(id, comment)})
        response.onSuccess {
            withdrawLiveData.postValue(it)
        }.onError {
            postWithdrawErrorLiveData.postValue(it)
        }
    }
    fun postRatingRequest(id: String, rating: Int) = viewModelScope.launch {
        val response = processCoroutine({ repository.postRating(id, rating)})
        response.onSuccess {
            ratingLiveData.postValue(it)
        }.onError {
            postRatingErrorLiveData.postValue(it)
        }
    }
}