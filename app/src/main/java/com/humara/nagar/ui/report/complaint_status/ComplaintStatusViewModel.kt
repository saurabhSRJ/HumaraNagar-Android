package com.humara.nagar.ui.report.complaint_status

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.network.ApiError
import com.humara.nagar.network.onError
import com.humara.nagar.network.onSuccess
import com.humara.nagar.ui.report.model.ComplaintStatus
import com.humara.nagar.ui.report.model.StatusResponse
import com.humara.nagar.utils.ComplaintsUtils
import com.humara.nagar.utils.SingleLiveEvent
import kotlinx.coroutines.launch

class ComplaintStatusViewModel(application: Application, private val savedStateHandle: SavedStateHandle) : BaseViewModel(application) {
    companion object {
        private const val RATING_KEY = "rating"
    }

    private val repository = ComplaintStatusRepository(application)
    private val _complaintStatusLiveData: MutableLiveData<ComplaintStatus> = MutableLiveData()
    val complaintStatusLiveData: LiveData<ComplaintStatus> = _complaintStatusLiveData
    private val _complaintStatusErrorLiveData: MutableLiveData<ApiError> by lazy { MutableLiveData() }
    val complaintStatusErrorLiveData: LiveData<ApiError> = _complaintStatusErrorLiveData
    private val _ratingSuccessLiveData: MutableLiveData<StatusResponse> = SingleLiveEvent()
    val ratingSuccessLiveData: LiveData<StatusResponse> = _ratingSuccessLiveData
    private val _ratingErrorLiveData: MutableLiveData<ApiError> by lazy { SingleLiveEvent() }
    val ratingErrorLiveData: LiveData<ApiError> = _ratingErrorLiveData
    private val _withdrawSuccessLiveData: MutableLiveData<StatusResponse> = SingleLiveEvent()
    val withdrawSuccessLiveData: LiveData<StatusResponse> = _withdrawSuccessLiveData
    private val _withdrawErrorLiveData: MutableLiveData<ApiError> by lazy { SingleLiveEvent() }
    val withdrawErrorLiveData: LiveData<ApiError> = _withdrawErrorLiveData
    private val _acknowledgementSuccessLiveData: MutableLiveData<StatusResponse> = SingleLiveEvent()
    val acknowledgementSuccessLiveData: LiveData<StatusResponse> = _acknowledgementSuccessLiveData
    private val _acknowledgementErrorLiveData: MutableLiveData<ApiError> by lazy { SingleLiveEvent() }
    val acknowledgementErrorLiveData: LiveData<ApiError> = _acknowledgementErrorLiveData
    private val _finishComplaintSuccessLiveData: MutableLiveData<StatusResponse> = SingleLiveEvent()
    val finishComplaintSuccessLiveData: LiveData<StatusResponse> = _finishComplaintSuccessLiveData
    private val _finishComplaintErrorLiveData: MutableLiveData<ApiError> by lazy { SingleLiveEvent() }
    val finishComplaintErrorLiveData: LiveData<ApiError> = _finishComplaintErrorLiveData

    private val complaintId: String = ComplaintStatusFragmentArgs.fromSavedStateHandle(savedStateHandle).complaintId //null safety is ensured by safe args
    val ratingData: LiveData<Int> = savedStateHandle.getLiveData(RATING_KEY, 0)
    val rating: Int get() = ratingData.value ?: 0
    private var state: String = ""

    init {
        getComplaintStatus(complaintId)
    }

    fun getComplaintStatus(id: String) = viewModelScope.launch {
        val response = processCoroutine({ repository.getComplaintStatus(id) })
        response.onSuccess {
            _complaintStatusLiveData.postValue(it)
            updateRatingData(it.rating)
            state = it.currentState
        }.onError {
            _complaintStatusErrorLiveData.postValue(it)
        }
    }

    fun updateRatingData(rating: Int) {
        savedStateHandle[RATING_KEY] = rating
    }

    fun onUserCommentReceived(comment: String, isUserAdmin: Boolean) {
        if (isUserAdmin && state == ComplaintsUtils.ComplaintState.SENT.currentState) {
            acknowledgeComplaint(comment)
        } else if (isUserAdmin && state == ComplaintsUtils.ComplaintState.IN_PROGRESS.currentState) {
            finishComplaint(comment)
        } else if (isUserAdmin.not() && (state == ComplaintsUtils.ComplaintState.SENT.currentState || state == ComplaintsUtils.ComplaintState.IN_PROGRESS.currentState)) {
            withdrawComplaint(comment)
        } else if (isUserAdmin.not() && rating > 0) {
            rateComplaintService(comment)
        }
    }

    private fun acknowledgeComplaint(comment: String) = viewModelScope.launch {
        val response = processCoroutine({ repository.acknowledgeComplaint(complaintId, comment) })
        response.onSuccess {
            _acknowledgementSuccessLiveData.postValue(it)
        }.onError {
            _acknowledgementErrorLiveData.postValue(it)
        }
    }

    private fun finishComplaint(comment: String) = viewModelScope.launch {
        val response = processCoroutine({ repository.finishComplaint(complaintId, comment) })
        response.onSuccess {
            _finishComplaintSuccessLiveData.postValue(it)
        }.onError {
            _finishComplaintErrorLiveData.postValue(it)
        }
    }

    private fun withdrawComplaint(comment: String) = viewModelScope.launch {
        val response = processCoroutine({ repository.withdrawComplaint(complaintId, comment) })
        response.onSuccess {
            _withdrawSuccessLiveData.postValue(it)
        }.onError {
            _withdrawErrorLiveData.postValue(it)
        }
    }

    private fun rateComplaintService(comment: String) = viewModelScope.launch {
        val response = processCoroutine({ repository.rateComplaintService(complaintId, rating, comment) })
        response.onSuccess {
            _ratingSuccessLiveData.postValue(it)
        }.onError {
            _ratingErrorLiveData.postValue(it)
            updateRatingData(0)
        }
    }
}