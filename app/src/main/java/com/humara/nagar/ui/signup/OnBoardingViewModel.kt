package com.humara.nagar.ui.signup

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.humara.nagar.Logger
import com.humara.nagar.analytics.AnalyticsTracker
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.network.onError
import com.humara.nagar.network.onSuccess
import com.humara.nagar.ui.signup.model.User
import com.humara.nagar.ui.signup.otp_verification.model.LoginRequest
import com.humara.nagar.ui.signup.profile_creation.model.ProfileCreationRequest
import com.humara.nagar.ui.signup.signup_or_login.model.SendOtpRequest
import com.humara.nagar.utils.SingleLiveEvent
import com.humara.nagar.utils.UserDataValidator
import kotlinx.coroutines.launch
import java.net.HttpURLConnection

class OnBoardingViewModel(application: Application) : BaseViewModel(application) {
    private val repository = OnBoardingRepository(application)
    private val _isUserUnderAnExistingRegistrationProcessLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val isUserUnderAnExistingRegistrationProcessLiveData: LiveData<Boolean> = _isUserUnderAnExistingRegistrationProcessLiveData
    private val _invalidMobileNumberLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val invalidMobileNumberLiveData: LiveData<Boolean> = _invalidMobileNumberLiveData
    private val _successfulUserCheckLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val successfulUserCheckLiveData: LiveData<Boolean> = _successfulUserCheckLiveData
    private val _successfulOtpResendLiveData: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }
    val successfulOtpResendLiveData: LiveData<Boolean> = _successfulOtpResendLiveData
    private val _invalidOtpLiveData: SingleLiveEvent<String> by lazy { SingleLiveEvent() }
    val invalidOtpLiveData: LiveData<String> = _invalidOtpLiveData
    private val _profileCreationRequiredLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val profileCreationRequiredLiveData: LiveData<Boolean> = _profileCreationRequiredLiveData
    private val _successfulUserSignupLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val successfulUserSignupLiveData: LiveData<Boolean> = _successfulUserSignupLiveData
    private val _showHomeScreenLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val showHomeScreenLiveData: LiveData<Boolean> = _showHomeScreenLiveData

    fun checkIfUserIsUnderOngoingRegistrationProcess() {
        val savedUserProfile = getUserPreference().userProfile
        _isUserUnderAnExistingRegistrationProcessLiveData.value = savedUserProfile != null
    }

    fun handleMobileNumberInput(mobileNumber: String) {
        if (UserDataValidator.isValidMobileNumber(mobileNumber)) {
            validateUserAndSendOtp(mobileNumber)
        } else {
            setInvalidMobileNumberLiveData(true)
        }
    }

    fun verifyOtp(otp: String) = viewModelScope.launch {
        val request = LoginRequest(getUserPreference().passCode, getUserPreference().mobileNumber, otp)
        val response = processCoroutine({ repository.verifyOtpAndLogin(request) })
        response.onSuccess { loginResponse ->
            with(getUserPreference()) {
                userProfile = User(userId = loginResponse.userId, mobileNumber = getUserPreference().mobileNumber)
                token = loginResponse.token
                refreshToken = loginResponse.refreshToken
                isUserLoggedIn = true
            }
            _profileCreationRequiredLiveData.postValue(loginResponse.isNewUser)
        }.onError {
            if (it.responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                _invalidOtpLiveData.postValue(null)
            } else {
                errorLiveData.postValue(it)
            }
        }
    }

    fun resendOtp() = viewModelScope.launch {
        val response = processCoroutine({ repository.sendOtp(SendOtpRequest(getUserPreference().mobileNumber)) })
        response.onSuccess {
            if (it.passcode.isNullOrEmpty()) {
                errorLiveData.postValue(null)
            } else {
                getUserPreference().passCode = it.passcode
                _successfulOtpResendLiveData.postValue(true)
            }
        }.onError {
            errorLiveData.postValue(it)
        }
    }

    fun updateSavedUserDetailsAndSignup(user: User) = viewModelScope.launch {
        val request = ProfileCreationRequest(user.userId, user.name, user.fatherOrSpouseName, user.dateOfBirth, user.gender, user.locality)
        val response = processCoroutine({ repository.signup(request) })
        response.onSuccess {
            getUserPreference().userProfile = user
            Logger.debugLog("Saved Profile: $user")
            _successfulUserSignupLiveData.postValue(true)
        }.onError {
            errorLiveData.postValue(it)
        }
    }

    fun setInvalidMobileNumberLiveData(isInvalid: Boolean) {
        _invalidMobileNumberLiveData.postValue(isInvalid)
    }

    private fun validateUserAndSendOtp(mobileNumber: String) = viewModelScope.launch {
        val response = processCoroutine({ repository.sendOtp(SendOtpRequest(mobileNumber)) })
        response.onSuccess {
            if (it.passcode.isNullOrEmpty()) {
                _successfulUserCheckLiveData.postValue(false)
            } else {
                getUserPreference().mobileNumber = mobileNumber
                getUserPreference().passCode = it.passcode
                _successfulUserCheckLiveData.postValue(true)
            }
        }.onError {
            errorLiveData.postValue(it)
        }
    }

    fun onUserOnBoard() {
        AnalyticsTracker.onUserOnBoard(getApplication())
        _showHomeScreenLiveData.value = true
    }
}