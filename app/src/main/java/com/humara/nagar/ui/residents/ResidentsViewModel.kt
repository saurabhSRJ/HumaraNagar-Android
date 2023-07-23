package com.humara.nagar.ui.residents

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
import com.humara.nagar.ui.residents.model.GetResidentsRequest
import com.humara.nagar.ui.residents.model.ResidentDetails
import com.humara.nagar.ui.residents.model.ResidentsResponse
import com.humara.nagar.utils.StringUtils.convertToLowerCase
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ResidentsViewModel(application: Application) : BaseViewModel(application) {
    private val repository = ResidentsRepository(application)
    private val appConfigRepository = AppConfigRepository(application)
    private val _initialDataLiveData: MutableLiveData<List<ResidentDetails>> by lazy { MutableLiveData() }
    val initialDataLiveData: LiveData<List<ResidentDetails>> = _initialDataLiveData
    private val _initialDataProgressLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val initialDataProgressLiveData: LiveData<Boolean> = _initialDataProgressLiveData
    private val _initialDataErrorLiveData: MutableLiveData<ApiError> by lazy { MutableLiveData() }
    val initialDataErrorLiveData: LiveData<ApiError> = _initialDataErrorLiveData
    private val _loadMoreDataLiveData: MutableLiveData<List<ResidentDetails>> by lazy { MutableLiveData() }
    val loadMoreDataLiveData: LiveData<List<ResidentDetails>> = _loadMoreDataLiveData
    private val _loadMoreDataProgressLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val loadMoreDataProgressLiveData: LiveData<Boolean> = _loadMoreDataProgressLiveData
    private val _loadMoreDataErrorLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val loadMoreDataErrorLiveData: LiveData<Boolean> = _loadMoreDataErrorLiveData
    private val _isSearchingLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val isSearchingLiveData: LiveData<Boolean> = _isSearchingLiveData
    private val _searchResultLiveData: MutableLiveData<Pair<List<ResidentDetails>, String>> by lazy { MutableLiveData() }
    val searchResultLiveData: LiveData<Pair<List<ResidentDetails>, String>> = _searchResultLiveData
    private val wardFilter = mutableListOf<Int>()

    private var currentPage: Int = 1
    private val limit = 20
    var canLoadMoreData = true

    init {
        initWardFilter()
    }

    fun getAllResidents(isLoadMoreCall: Boolean = false) = viewModelScope.launch {
        if (isLoadMoreCall) currentPage++ else resetPaginationState()
        val request = GetResidentsRequest(wardFilter)
        val response = processCoroutine(
            { repository.getAllResidents(currentPage, limit, request) },
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

    fun searchResidentList(searchText: String) = viewModelScope.launch {
        val request = GetResidentsRequest(wardFilter)
        val response = processCoroutine({ repository.searchResidentList(searchText.convertToLowerCase(), request) }, progressLiveData = _isSearchingLiveData)
        response.onSuccess {
            _searchResultLiveData.postValue(Pair(it.residentDetails, searchText))
        }.onError {
            errorLiveData.postValue(it)
        }
    }

    private fun initWardFilter() = viewModelScope.launch {
        if (Role.shouldShowResidentsFromAllWards(getUserPreference().role?.id ?: 0)) {
            wardFilter.clear()
            getAllResidents()
        } else {
            val wardId = async { appConfigRepository.getWardId(getUserPreference().ward!!) }
            wardFilter.add(wardId.await())
            getAllResidents()
        }
    }

    private fun processInitialResponse(response: ResidentsResponse) {
        setPaginationState(response)
        _initialDataLiveData.postValue(response.residentDetails)
    }

    private fun processLoadMoreResponse(response: ResidentsResponse) {
        setPaginationState(response)
        _loadMoreDataLiveData.postValue(response.residentDetails)
    }

    private fun setPaginationState(response: ResidentsResponse) {
        canLoadMoreData = response.page < response.totalPages
    }

    private fun resetPaginationState() {
        currentPage = 1
        canLoadMoreData = true
    }
}