package com.humara.nagar.ui.residents

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.network.ApiError
import com.humara.nagar.network.onError
import com.humara.nagar.network.onSuccess
import com.humara.nagar.ui.residents.model.FiltersResponse
import com.humara.nagar.ui.residents.model.ResidentsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ResidentsViewModel(application: Application) : BaseViewModel(application) {

    private val _residentsResponse = MutableLiveData<ResidentsResponse>()
    val residentsResponse get() = _residentsResponse
    private val _filtersResponse = MutableLiveData<FiltersResponse>()
    val filtersResponse get() = _filtersResponse
    private val repository = ResidentsRepository(application)
    val residentErrorLiveData = MutableLiveData<ApiError>()
    val chipFilterErrorLiveData = MutableLiveData<ApiError>()

    fun fetchAllResidents() =
        viewModelScope.launch(Dispatchers.IO) {
            val response = processCoroutine({ repository.requestAllResidentsList() })
            response.onSuccess {
                _residentsResponse.postValue(it)
            }.onError {
                residentErrorLiveData.postValue(it)
            }
        }

    fun fetchAllFilters() =
        viewModelScope.launch(Dispatchers.IO) {
            val response = processCoroutine({ repository.requestAllFilters() })
            response.onSuccess {
                _filtersResponse.postValue(it)
            }.onError {
                chipFilterErrorLiveData.postValue(it)
            }
        }
}