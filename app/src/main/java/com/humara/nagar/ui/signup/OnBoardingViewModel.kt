package com.humara.nagar.ui.signup

import android.app.Application
import android.net.Uri
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
    private val _showAddProfileImageScreenLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val showAddProfileImageScreenLiveData: LiveData<Boolean> = _showAddProfileImageScreenLiveData
    private val _addProfileImageStatusLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val addProfileImageStatusLiveData: LiveData<Boolean> = _addProfileImageStatusLiveData
    private val _showHomeScreenLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val showHomeScreenLiveData: LiveData<Boolean> = _showHomeScreenLiveData
    private var isNewUser: Boolean = true

    fun checkIfUserIsUnderOngoingRegistrationProcess() {
        _isUserUnderAnExistingRegistrationProcessLiveData.value = getUserPreference().userId != 0L
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
            getUserPreference().run {
                token = loginResponse.token
                refreshToken = loginResponse.refreshToken
                userId = loginResponse.userId
                loginResponse.userInfo?.let { user ->
                    val userInfo = User(user.userId, user.getFullName(), getUserPreference().mobileNumber, user.fatherOrSpouseName, user.gender, user.locality)
                    isUserLoggedIn = true
                    userProfile = userInfo
                    Logger.debugLog("Saved Profile: $userInfo")
                }
            }
            isNewUser = loginResponse.isNewUser
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

    fun updateSavedUserDetailsAndSignup(request: ProfileCreationRequest) = viewModelScope.launch {
        val response = processCoroutine({ repository.signup(request) })
        response.onSuccess {
            // TODO: try to sync this from backend response instead of using the app request body. Similar to login api
            val createdUser = User(request.userId, request.name, getUserPreference().mobileNumber, request.fatherOrSpouseName, request.gender, request.locality)
            getUserPreference().userProfile = createdUser
            getUserPreference().isUserLoggedIn = true
            getUserPreference().profileImage = it.image
            Logger.debugLog("Saved Profile: $createdUser")
            _successfulUserSignupLiveData.postValue(true)
        }.onError {
            errorLiveData.postValue(it)
        }
    }

    fun updateProfileImage(imageUri: Uri) = viewModelScope.launch {
        val response = processCoroutine({ repository.updateProfileImage(imageUri) })
        response.onSuccess {
            getUserPreference().profileImage = it.image
            _addProfileImageStatusLiveData.postValue(true)
        }.onError {
            _addProfileImageStatusLiveData.postValue(false)
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
        if (isNewUser) {
            _showAddProfileImageScreenLiveData.value = true
        } else {
            _showHomeScreenLiveData.value = true
        }
    }
}