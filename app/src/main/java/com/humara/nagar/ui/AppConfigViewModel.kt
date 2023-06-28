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
import com.humara.nagar.ui.signup.model.*
import com.humara.nagar.utils.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppConfigViewModel(application: Application) : BaseViewModel(application) {
    private val repository = AppConfigRepository(application)
    private val fcmRepository = FcmTokenUploadRepository(application)
    private val _appConfigAndUserRefDataSuccessLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val appConfigAndUserRefDataSuccessLiveData: LiveData<Boolean> = _appConfigAndUserRefDataSuccessLiveData
    private val _appConfigSuccessLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val appConfigSuccessLiveData: LiveData<Boolean> = _appConfigSuccessLiveData
    private val _userRefDataSuccessLiveData: MutableLiveData<Boolean> by lazy { (MutableLiveData()) }
    val userRefDataSuccessLiveData: LiveData<Boolean> = _userRefDataSuccessLiveData
    private val _roleDetailsLiveData: MutableLiveData<List<RoleDetails>> by lazy { SingleLiveEvent() }
    val roleDetailsLiveData: LiveData<List<RoleDetails>> = _roleDetailsLiveData
    private val _wardDetailsLiveData: MutableLiveData<List<WardDetails>> by lazy { SingleLiveEvent() }
    val wardDetailsLiveData: LiveData<List<WardDetails>> = _wardDetailsLiveData
    private val _genderDetailsLiveData: MutableLiveData<List<GenderDetails>> by lazy { SingleLiveEvent() }
    val genderDetailsLiveData: LiveData<List<GenderDetails>> = _genderDetailsLiveData
    private val _complaintCategoriesLiveData: MutableLiveData<List<String>> by lazy { SingleLiveEvent() }
    val complaintCategoriesLiveData: LiveData<List<String>> = _complaintCategoriesLiveData
    private val _logoutLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val logoutLiveData: LiveData<Boolean> = _logoutLiveData

    fun getAppConfigAndUserReferenceData() = viewModelScope.launch {
        val appConfigDeferred = async { processCoroutine({ repository.getAppConfig() }) }
        val userRefDataDeferred = async { processCoroutine({ repository.getUserReferenceDetails(UserReferenceDataRequest(getUserPreference().userId)) }) }
        val appConfigResult = appConfigDeferred.await()
        val userRefDataResult = userRefDataDeferred.await()
        userRefDataResult.onSuccess { refData ->
            repository.insertCategories(refData.categories)
            repository.insertRoles(refData.roles)
            repository.insertWards(refData.wards)
            repository.insertGenders(refData.genders)
            repository.insertFeedFilters(refData.feedFilters)
            appConfigResult.onSuccess {
                getUserPreference().role = RoleDetails(it.roleId, it.role)
                getUserPreference().profileImage = it.image
                getUserPreference().userName = it.name
                _appConfigAndUserRefDataSuccessLiveData.postValue(true)
            }.onError {
                errorLiveData.postValue(it)
            }
        }.onError {
            errorLiveData.postValue(it)
        }
        Logger.debugLog("User role: ${getUserPreference().role?.name}")
    }

    fun getAppConfig() = viewModelScope.launch {
        val response = processCoroutine({ repository.getAppConfig() })
        response.onSuccess {
            getUserPreference().role = RoleDetails(it.roleId, it.role)
            getUserPreference().userName = it.name
            getUserPreference().profileImage = it.image
            _appConfigSuccessLiveData.postValue(true)
        }.onError {
            errorLiveData.postValue(it)
        }
    }

    fun getUserReferenceData() = viewModelScope.launch {
        val response = processCoroutine({ repository.getUserReferenceDetails(UserReferenceDataRequest(getUserPreference().userId)) })
        response.onSuccess { refData ->
            repository.insertCategories(refData.categories)
            repository.insertRoles(refData.roles)
            repository.insertWards(refData.wards)
            repository.insertGenders(refData.genders)
            repository.insertFeedFilters(refData.feedFilters)
            _userRefDataSuccessLiveData.postValue(true)
        }.onError {
            errorLiveData.postValue(it)
        }
    }

    fun getRoles() = viewModelScope.launch {
        val roles = async { repository.getAllRoles() }
        _roleDetailsLiveData.postValue(roles.await())
    }

    fun getWards() = viewModelScope.launch {
        val wards = async { repository.getAllWards() }
        _wardDetailsLiveData.postValue(wards.await())
    }

    fun getGenders() = viewModelScope.launch {
        val genders = async { repository.getAllGenders() }
        _genderDetailsLiveData.postValue(genders.await())
    }

    fun getComplaintCategories() = viewModelScope.launch {
        val categories = async { repository.getComplaintCategories() }
        _complaintCategoriesLiveData.postValue(categories.await().map { it.name })
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
        var newToken = ""
        try {
            newToken = fetchTokenTask.await().result
        } catch (e: Exception) {
            Logger.logException("AppConfig", e, Logger.LogLevel.ERROR, true)
            return@launch
        }
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