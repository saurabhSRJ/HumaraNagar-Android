package com.humara.nagar.ui.residents

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.network.ApiError
import com.humara.nagar.network.onError
import com.humara.nagar.network.onSuccess
import com.humara.nagar.ui.residents.model.ResidentDetails
import com.humara.nagar.ui.residents.model.ResidentsResponse
import com.humara.nagar.utils.StringUtils.convertToLowerCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class ResidentsViewModel(application: Application) : BaseViewModel(application) {
    private val repository = ResidentsRepository(application)
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
    private val _searchErrorLiveData: MutableLiveData<ApiError> by lazy { MutableLiveData() }
    val searchErrorLiveData: LiveData<ApiError> = _searchErrorLiveData

    private var currentPage: Int = 1
    private val limit = 20
    var canLoadMoreData = true

    init {
        getAllResidents()
    }

    //  Random Data generation

    private fun generateRandomString(length: Int): String {
        val allowedChars = ('a'..'z') + ' '
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private fun generateRandomInteger(minValue: Int, maxValue: Int): Int {
        return Random.nextInt(minValue, maxValue + 1)
    }

    private fun generateRandomPhoneNumber(): String {
        val allowedChars = ('0'..'9')
        return (1..10)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private fun generateRandomObject(page: Int): ResidentsResponse {
        val totalCount = generateRandomInteger(1, 100)
        val totalPages = 3

        val residents = mutableListOf<ResidentDetails>()
        repeat(limit) {
            val nameSize = generateRandomInteger(5, 30)
            val resident = ResidentDetails(
                name = generateRandomString(nameSize),
                fathersName = generateRandomString(nameSize),
                age = generateRandomInteger(18, 80),
                phoneNumber = generateRandomPhoneNumber(),
                image = generateRandomString(10),
                role = generateRandomString(10),
                ward = generateRandomInteger(1, 10).toString()
            )
            residents.add(resident)
        }

        return ResidentsResponse(
            totalCount = totalCount,
            totalPages = totalPages,
            page = page,
            residentDetails = residents
        )
    }

    // Generate a random object
    private val initialResponse = generateRandomObject(1)
    private val loadMoreResponse = generateRandomObject(2)

    //

    fun getAllResidents() = viewModelScope.launch {
//        val response = processCoroutine(
//            { repository.getAllResidents(currentPage, limit, "") },
//            progressLiveData = if (currentPage == 1) _initialDataProgressLiveData else _loadMoreDataProgressLiveData
//        )
        if (currentPage == 1) _initialDataProgressLiveData.postValue(true) else _loadMoreDataProgressLiveData.postValue(true)
        delay(1000)
        if (currentPage == 1) _initialDataProgressLiveData.postValue(false) else _loadMoreDataProgressLiveData.postValue(false)
        if (currentPage == 1) {
            processInitialResponse(initialResponse)
        } else {
            processLoadMoreResponse(loadMoreResponse)
        }
//        response.onSuccess {
//            if (currentPage == 1) processInitialResponse(it)
//            else processLoadMoreResponse(it)
//        }.onError {
//            if (currentPage == 1) _initialDataErrorLiveData.postValue(it)
//            else _loadMoreDataErrorLiveData.postValue(true)
//        }.onException {
//            if (currentPage > 1) _loadMoreDataErrorLiveData.postValue(true)
//        }
    }

    fun searchResidentList(searchText: String) = viewModelScope.launch {
//        val response = processCoroutine({ repository.getAllResidents(1, 50, searchText.convertToLowerCase()) }, progressLiveData = _isSearchingLiveData)
//        response.onSuccess {
//            _searchResultLiveData.postValue(Pair(it.residentDetails, searchText))
//        }.onError {
//            _searchErrorLiveData.postValue(it)
//        }
        _isSearchingLiveData.postValue(true)
        delay(2000)
        _searchResultLiveData.postValue(Pair(initialResponse.residentDetails, searchText))
        _isSearchingLiveData.postValue(false)
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
        currentPage = response.page + 1
        canLoadMoreData = response.page < response.totalPages
    }

    fun resetPaginationState() {
        currentPage = 1
        canLoadMoreData = true
    }
}