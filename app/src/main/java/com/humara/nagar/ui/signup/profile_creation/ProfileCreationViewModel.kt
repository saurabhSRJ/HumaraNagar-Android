package com.humara.nagar.ui.signup.profile_creation

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.network.onError
import com.humara.nagar.network.onSuccess
import com.humara.nagar.ui.AppConfigRepository
import com.humara.nagar.ui.signup.model.AppConfigRequest
import com.humara.nagar.ui.signup.model.Gender
import com.humara.nagar.ui.signup.model.User
import com.humara.nagar.ui.signup.model.UserReferenceDataRequest
import com.humara.nagar.utils.DateTimeUtils
import com.humara.nagar.utils.SingleLiveEvent
import com.humara.nagar.utils.UserDataValidator
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ProfileCreationViewModel(application: Application, private val savedStateHandle: SavedStateHandle) : BaseViewModel(application) {
    companion object {
        private const val USERNAME_KEY = "username"
        private const val PARENT_NAME_KEY = "parent"
        private const val DOB_KEY = "dob"
        private const val GENDER_KEY = "gender"
        private const val LOCALITY_KEY = "locality"
        private const val SUBMIT_BUTTON_KEY = "submit"
    }

    private val _invalidDateOfBirthLiveData: MutableLiveData<Boolean> by lazy { SingleLiveEvent() }
    val invalidDateOfBirthLiveData: LiveData<Boolean> = _invalidDateOfBirthLiveData
    private val _userLocalitiesLiveData: MutableLiveData<List<String>> by lazy { SingleLiveEvent() }
    val userLocalitiesLiveData: LiveData<List<String>> = _userLocalitiesLiveData
    private val appConfigRepository = AppConfigRepository(application)

    fun getAppConfigAndUserReferenceData() = viewModelScope.launch {
        val appConfigDeferred = async { appConfigRepository.getAppConfig(AppConfigRequest(getUserPreference().userId)) }
        val userRefDataDeferred = async { appConfigRepository.getUserReferenceDetails(UserReferenceDataRequest(getUserPreference().userId)) }
        val appConfigResult = appConfigDeferred.await()
        val userRefDataResult = userRefDataDeferred.await()
        appConfigResult.onSuccess {
            getUserPreference().role = it.role
            getUserPreference().wardId = it.wardId
        }.onError {
            errorLiveData.postValue(it)
        }
        userRefDataResult.onSuccess {
            val filteredLocalities = it.localities.filter { locality -> locality.wardId == getUserPreference().wardId }.map { it.name }
            _userLocalitiesLiveData.postValue(filteredLocalities)
        }.onError {
            errorLiveData.postValue(it)
        }
    }

    fun setDateOfBirth(dob: String) {
        if (UserDataValidator.isValidDateOfBirth(dob)) {
            savedStateHandle[DOB_KEY] = dob
        } else {
            _invalidDateOfBirthLiveData.value = true
        }
        updateSubmitButtonState()
    }

    fun getDateOfBirth(): LiveData<String> = savedStateHandle.getLiveData(DOB_KEY)

    fun setUserName(name: String) {
        savedStateHandle[USERNAME_KEY] = name
        updateSubmitButtonState()
    }

    private fun getUserName(): String? = savedStateHandle[USERNAME_KEY]

    fun setParentName(name: String) {
        savedStateHandle[PARENT_NAME_KEY] = name
        updateSubmitButtonState()
    }

    private fun getParentName(): String? = savedStateHandle[PARENT_NAME_KEY]

    fun setLocality(locality: String) {
        savedStateHandle[LOCALITY_KEY] = locality
        updateSubmitButtonState()
    }

    private fun getLocality(): String? = savedStateHandle[LOCALITY_KEY]

    fun setGender(gender: String) {
        savedStateHandle[GENDER_KEY] = gender
    }

    private fun getGender(): String = savedStateHandle[GENDER_KEY] ?: Gender.MALE.name

    fun getUserObjectWithCollectedData(): User {
        val user: User = getUserPreference().userProfile!!.apply {
            name = getUserName()!!
            fatherOrSpouseName = getParentName()!!
            dateOfBirth = DateTimeUtils.convertDateFormat(getDateOfBirth().value.toString(), "dd-MM-yyyy", "yyyy-MM-dd")
            locality = getLocality()!!
            gender = getGender()
        }
        return user
    }

    private fun updateSubmitButtonState() {
        val anyRequiredFieldEmpty = getUserName().isNullOrEmpty() || getDateOfBirth().value.isNullOrEmpty() || getParentName().isNullOrEmpty() || getLocality().isNullOrEmpty()
        savedStateHandle[SUBMIT_BUTTON_KEY] = anyRequiredFieldEmpty.not()
    }

    fun getSubmitButtonState(): LiveData<Boolean> = savedStateHandle.getLiveData(SUBMIT_BUTTON_KEY, false)
}