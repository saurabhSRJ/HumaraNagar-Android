package com.humara.nagar.ui.signup

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.humara.nagar.Logger
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.network.BaseRepository
import com.humara.nagar.network.onError
import com.humara.nagar.network.onSuccess
import com.humara.nagar.ui.signup.model.User
import com.humara.nagar.utils.SingleLiveEvent
import com.humara.nagar.utils.UserDataValidator
import kotlinx.coroutines.launch

class OnBoardingViewModel(application: Application) : BaseViewModel(application) {
    private val _isUserUnderAnExistingRegistrationProcessLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val isUserUnderAnExistingRegistrationProcessLiveData: LiveData<Boolean> = _isUserUnderAnExistingRegistrationProcessLiveData
    private val _invalidMobileNumberLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val invalidMobileNumberLiveData: LiveData<Boolean> = _invalidMobileNumberLiveData
    private val _successfulUserCheckLiveData: MutableLiveData<User> by lazy { MutableLiveData() }
    val successfulUserCheckLiveData: LiveData<User> = _successfulUserCheckLiveData
    private val _successfulOtpResendLiveData: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }
    val successfulOtpResendLiveData: LiveData<Boolean> = _successfulOtpResendLiveData
    private val _invalidOtpLiveData: SingleLiveEvent<String> by lazy { SingleLiveEvent() }
    val invalidOtpLiveData: LiveData<String> = _invalidOtpLiveData
    private val _profileCreationRequiredLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val profileCreationRequiredLiveData: LiveData<Boolean> = _profileCreationRequiredLiveData
    private val _successfulUserLoginLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val successfulUserLoginLiveData: LiveData<Boolean> = _successfulUserLoginLiveData
    private val repository = BaseRepository(application)

    fun checkIfUserIsUnderOngoingRegistrationProcess() {
        val savedUserProfile = getUserPreference().userProfile
        val isUserUnderAnExistingRegistrationProcess = savedUserProfile != null && savedUserProfile.name.isEmpty()
        _isUserUnderAnExistingRegistrationProcessLiveData.value = false
//        _isUserUnderAnExistingRegistrationProcessLiveData.value = isUserUnderAnExistingRegistrationProcess
    }

    fun handleMobileNumberInput(mobileNumber: String) {
        if (UserDataValidator.isValidMobileNumber(mobileNumber)) {
            validateUser(mobileNumber)
        } else {
            setInvalidMobileNumberLiveData(true)
        }
    }

    fun verifyOtp(otp: String) = viewModelScope.launch {
        //TODO: Add verifyOTP API
        _profileCreationRequiredLiveData.postValue(true)
//        _invalidOtpLiveData.postValue(null)
    }

    fun resendOtp() = viewModelScope.launch {
        //TODO: Add resendOtp API
        val response = processCoroutine({ repository.getUsers() })
        response.onSuccess {
            _successfulOtpResendLiveData.postValue(true)
        }.onError {
            errorLiveData.postValue(it)
        }
    }

    fun updateUserDetails(user: User) = viewModelScope.launch {
        //TODO: Add profileCreation and config API
        val response = processCoroutine({ repository.getUsers() })
        response.onSuccess {
            getUserPreference().userProfile = user
            Logger.debugLog("Saved Profile", user.toString())
            _successfulUserLoginLiveData.postValue(true)
        }
    }

    fun setInvalidMobileNumberLiveData(isInvalid: Boolean) {
        _invalidMobileNumberLiveData.postValue(isInvalid)
    }

    private fun validateUser(mobileNumber: String) {
        //TODO: Add verifyUser API
        val userProfile = User(mobileNumber = mobileNumber)
        getUserPreference().userProfile = userProfile
        getUserPreference().mobileNumber = mobileNumber
        _successfulUserCheckLiveData.postValue(userProfile)
    }
}