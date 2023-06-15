package com.humara.nagar.ui

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.humara.nagar.Logger
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.fcm.FcmTokenUploadRepository
import com.humara.nagar.network.onError
import com.humara.nagar.network.onSuccess
import com.humara.nagar.ui.signup.model.AppConfigRequest
import com.humara.nagar.ui.signup.model.LogoutRequest
import com.humara.nagar.ui.signup.model.Role
import com.humara.nagar.ui.signup.model.UserReferenceDataRequest
import com.humara.nagar.utils.SingleLiveEvent
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class AppConfigViewModel(application: Application) : BaseViewModel(application) {
    private val repository = AppConfigRepository(application)
    private val fcmRepository = FcmTokenUploadRepository(application)
    private val _appConfigSuccessLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val appConfigSuccessLiveData: LiveData<Boolean> = _appConfigSuccessLiveData
    private val _userLocalitiesLiveData: MutableLiveData<List<String>> by lazy { SingleLiveEvent() }
    val userLocalitiesLiveData: LiveData<List<String>> = _userLocalitiesLiveData
    private val _complaintCategoriesLiveData: MutableLiveData<List<String>> by lazy { SingleLiveEvent() }
    val complaintCategoriesLiveData: LiveData<List<String>> = _complaintCategoriesLiveData
    private val _logoutLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val logoutLiveData: LiveData<Boolean> = _logoutLiveData

    fun getAppConfigAndUserReferenceData() = viewModelScope.launch {
        val appConfigDeferred = async { processCoroutine({ repository.getAppConfig(AppConfigRequest(getUserPreference().userId)) }) }
        val userRefDataDeferred = async { processCoroutine({ repository.getUserReferenceDetails(UserReferenceDataRequest(getUserPreference().userId)) }) }
        val appConfigResult = appConfigDeferred.await()
        val userRefDataResult = userRefDataDeferred.await()
        userRefDataResult.onSuccess { refData ->
            repository.insertCategories(refData.categories)
            repository.insertLocalities(refData.localities)
            appConfigResult.onSuccess {
                getUserPreference().role = it.role
                getUserPreference().ward = it.ward
                getUserPreference().wardId = it.wardId
                getUserPreference().profileImage = it.image
                getUserPreference().isAdminUser = it.role == Role.ADMIN.role
                _appConfigSuccessLiveData.postValue(true)
            }.onError {
                errorLiveData.postValue(it)
            }
        }.onError {
            errorLiveData.postValue(it)
        }
        Logger.debugLog("User role: ${getUserPreference().role}. isAdmin: ${getUserPreference().isAdminUser}")
    }

    fun getUserLocalities() = viewModelScope.launch {
        val localities = repository.getUserLocalities(getUserPreference().wardId)
        _userLocalitiesLiveData.postValue(localities.map { it.name })
    }

    fun getComplaintCategories() = viewModelScope.launch {
        val categories = repository.getComplaintCategories()
        _complaintCategoriesLiveData.postValue(categories.map { it.name })
    }

    fun logout() = viewModelScope.launch {
        val response = processCoroutine({ repository.logout(LogoutRequest(getUserPreference().userId, getUserPreference().refreshToken)) })
        //Logging out user in any case for now.
        response.onSuccess {
            _logoutLiveData.postValue(true)
        }.onError {
            _logoutLiveData.postValue(true)
        }
    }

    fun fetchFcmTokenAndUpdateToServerIfRequired() = viewModelScope.launch {
        val fetchTokenTask = async { fcmRepository.fetchFcmToken() }
        val storedToken = getUserPreference().fcmToken
        val isTokenUpdated = getUserPreference().fcmTokenUpdated
        val newToken = fetchTokenTask.await().result
        Logger.debugLog("fcm token: $newToken")
        if (isTokenUpdated && storedToken != newToken) {
            val response = processCoroutine({ fcmRepository.updateFcmTokenToServer(newToken) })
            response.onSuccess {
                getUserPreference().fcmToken = newToken
                getUserPreference().fcmTokenUpdated = false
            }.onError {
                //NA
            }
        }
    }
}