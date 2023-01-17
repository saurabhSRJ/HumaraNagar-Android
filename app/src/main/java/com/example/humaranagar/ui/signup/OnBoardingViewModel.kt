package com.example.humaranagar.ui.signup

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.humaranagar.base.BaseViewModel
import com.example.humaranagar.network.BaseRepository
import com.example.humaranagar.ui.signup.model.User
import com.example.humaranagar.utils.UserDataValidator

class OnBoardingViewModel(application: Application, repository: BaseRepository) : BaseViewModel(application, repository) {
    private val _isUserUnderAnExistingRegistrationProcessLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val isUserUnderAnExistingRegistrationProcessLiveData: LiveData<Boolean> = _isUserUnderAnExistingRegistrationProcessLiveData
    private val _invalidMobileNumberLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val invalidMobileNumberLiveData: LiveData<Boolean> = _invalidMobileNumberLiveData
    private val _successfulUserCheckLiveData: MutableLiveData<User> by lazy { MutableLiveData() }
    val successfulUserCheckLiveData: LiveData<User> = _successfulUserCheckLiveData
    private val _successfulOtpResendLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val successfulOtpResendLiveData: LiveData<Boolean> = _successfulOtpResendLiveData
    private val _invalidOtpLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val invalidOtpLiveData: LiveData<Boolean> = _invalidOtpLiveData
    private val _profileCreationRequiredLiveData: MutableLiveData<Boolean>by lazy { MutableLiveData() }
    val profileCreationRequiredLiveData: LiveData<Boolean> = _profileCreationRequiredLiveData
    private val _successfulUserLoginLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val successfulUserLoginLiveData: LiveData<Boolean> = _successfulUserLoginLiveData

    fun checkIfUserIsUnderOngoingRegistrationProcess() {
        val savedUserProfile = getUserPreference().userProfile
        val isUserUnderAnExistingRegistrationProcess = savedUserProfile != null && savedUserProfile.name.isEmpty()
        _isUserUnderAnExistingRegistrationProcessLiveData.value = false
//        _isUserUnderAnExistingRegistrationProcessLiveData.value = isUserUnderAnExistingRegistrationProcess
    }

    fun handleMobileNumberInput(mobileNumber: String) {
        if (UserDataValidator.isValidMobileNumber(mobileNumber)) {
            progressLiveData.postValue(true)
            validateUser(mobileNumber)
        } else {
            setInvalidMobileNumberLiveData(true)
        }
    }

    fun verifyOtp(otp: String) {
        //TODO: Add verifyOTP API
        progressLiveData.postValue(true)
        _profileCreationRequiredLiveData.postValue(true)
    }

    fun resendOtp() {
        //TODO: Add resendOtp API
        progressLiveData.postValue(true)
        _successfulOtpResendLiveData.postValue(true)
    }

    fun updateUserDetails(user: User) {
        progressLiveData.postValue(true)
        //TODO: Add profileCreation API
        getUserPreference().userProfile = user
        Log.d("Saved Profile", user.toString())
        _successfulUserLoginLiveData.postValue(true)
    }

    fun setInvalidOtpLiveData(isInvalid: Boolean) {
        _invalidOtpLiveData.postValue(isInvalid)
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