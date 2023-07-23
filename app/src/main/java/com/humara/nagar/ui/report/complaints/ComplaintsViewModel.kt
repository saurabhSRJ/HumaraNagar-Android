package com.humara.nagar.ui.report.complaints

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.humara.nagar.Role
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.network.ApiError
import com.humara.nagar.network.onError
import com.humara.nagar.network.onException
import com.humara.nagar.network.onSuccess
import com.humara.nagar.ui.AppConfigRepository
import com.humara.nagar.ui.report.model.AllComplaintsResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ComplaintsViewModel(application: Application) : BaseViewModel(application) {
    private val repository = ComplaintsRepository(application)
    private val appConfigRepository = AppConfigRepository(application)

    private val _initialDataLiveData: MutableLiveData<AllComplaintsResponse> by lazy { MutableLiveData() }
    val initialDataLiveData: LiveData<AllComplaintsResponse> = _initialDataLiveData
    private val _initialDataProgressLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val initialDataProgressLiveData: LiveData<Boolean> = _initialDataProgressLiveData
    private val _initialDataErrorLiveData: MutableLiveData<ApiError> by lazy { MutableLiveData() }
    val initialDataErrorLiveData: LiveData<ApiError> = _initialDataErrorLiveData
    private val _loadMoreDataLiveData: MutableLiveData<AllComplaintsResponse> by lazy { MutableLiveData() }
    val loadMoreDataLiveData: LiveData<AllComplaintsResponse> = _loadMoreDataLiveData
    private val _loadMoreDataProgressLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val loadMoreDataProgressLiveData: LiveData<Boolean> = _loadMoreDataProgressLiveData
    private val _loadMoreDataErrorLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val loadMoreDataErrorLiveData: LiveData<Boolean> = _loadMoreDataErrorLiveData
    private val _selectedFilterLiveData: MutableLiveData<Int> by lazy { MutableLiveData(1) }
    val selectedFilterLiveData: LiveData<Int> = _selectedFilterLiveData

    private var wardFilter = 0
    private var currentPage: Int = 1
    private val limit = 10
    var canLoadMoreData = true

    init {
        initWardFilter()
    }

    private fun initWardFilter() = viewModelScope.launch {
        if (Role.shouldShowResidentsFromAllWards(getUserPreference().role?.id ?: 0)) {
            getComplaints()
        } else {
            val wardId = async { appConfigRepository.getWardId(getUserPreference().ward!!) }
            wardFilter = wardId.await()
            getComplaints()
        }
    }

    fun setSelectedFilterId(id: Int) {
        _selectedFilterLiveData.value = id
    }

    fun getComplaints(isLoadMoreCall: Boolean = false) = viewModelScope.launch {
        if (isLoadMoreCall) currentPage++ else resetPaginationState()
        val response = processCoroutine(
            { repository.getComplaints(currentPage, limit, wardFilter, selectedFilterLiveData.value!!) },
            progressLiveData = if (isLoadMoreCall) _loadMoreDataProgressLiveData else _initialDataProgressLiveData
        )
        response.onSuccess {
            if (isLoadMoreCall) processLoadMoreResponse(it)
            else processInitialResponse(it)
        }.onError {
            if (isLoadMoreCall) _loadMoreDataErrorLiveData.postValue(true)
            else _initialDataErrorLiveData.postValue(it)
        }.onException {
            if (isLoadMoreCall) _loadMoreDataErrorLiveData.postValue(true)
        }
    }

    private fun processInitialResponse(response: AllComplaintsResponse) {
        setPaginationState(response)
        _initialDataLiveData.postValue(response)
    }

    private fun processLoadMoreResponse(response: AllComplaintsResponse) {
        setPaginationState(response)
        _loadMoreDataLiveData.postValue(response)
    }

    private fun setPaginationState(response: AllComplaintsResponse) {
        canLoadMoreData = response.page < response.totalPages
    }

    private fun resetPaginationState() {
        currentPage = 1
        canLoadMoreData = true
    }
}