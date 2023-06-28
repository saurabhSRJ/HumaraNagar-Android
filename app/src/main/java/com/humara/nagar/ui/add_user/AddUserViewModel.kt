package com.humara.nagar.ui.add_user

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.constants.Constants
import com.humara.nagar.network.onError
import com.humara.nagar.network.onSuccess
import com.humara.nagar.ui.add_user.model.AddUserDetailsRequest
import com.humara.nagar.ui.signup.signup_or_login.model.SendOtpRequest
import com.humara.nagar.utils.DateTimeUtils
import com.humara.nagar.utils.SingleLiveEvent
import com.humara.nagar.utils.UserDataValidator
import kotlinx.coroutines.launch
import java.net.HttpURLConnection

class AddUserViewModel(application: Application, private val savedStateHandle: SavedStateHandle) : BaseViewModel(application) {
    companion object {
        private const val OTP = "otp"
        private const val MOBILE_NUMBER = "mobile_number"
    }

    private val repository = AddUserRepository(application)
    private val _invalidMobileNumberLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val invalidMobileNumberLiveData: LiveData<Boolean> = _invalidMobileNumberLiveData
    private val _successfulUserCheckLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val successfulUserCheckLiveData: LiveData<Boolean> = _successfulUserCheckLiveData
    private val _duplicateUserLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val duplicateUserLiveData: LiveData<Boolean> = _duplicateUserLiveData
    private val _successfulOtpResendLiveData: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }
    val successfulOtpResendLiveData: LiveData<Boolean> = _successfulOtpResendLiveData
    private val _invalidOtpLiveData: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }
    val invalidOtpLiveData: LiveData<Boolean> = _invalidOtpLiveData
    private val _otpExpiredLiveData: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }
    val otpExpiredLiveData: LiveData<Boolean> = _otpExpiredLiveData
    private val _profileCreationRequiredLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val profileCreationRequiredLiveData: LiveData<Boolean> = _profileCreationRequiredLiveData
    private val _successfulUserAdditionLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val successfulUserAdditionLiveData: LiveData<Boolean> = _successfulUserAdditionLiveData

    val mobileNumberLiveData: LiveData<String> = savedStateHandle.getLiveData(MOBILE_NUMBER)
    private val otpLiveData: LiveData<Pair<String, Long>> = savedStateHandle.getLiveData(OTP)

    fun handleMobileNumberInput(mobileNumber: String) {
        if (UserDataValidator.isValidMobileNumber(mobileNumber)) {
            validateUserAndSendOtp(mobileNumber)
        } else {
            setInvalidMobileNumberLiveData(true)
        }
    }

    private fun validateUserAndSendOtp(mobileNumber: String) = viewModelScope.launch {
        val response = processCoroutine({ repository.sendOtp(SendOtpRequest(mobileNumber)) })
        response.onSuccess {
            savedStateHandle[MOBILE_NUMBER] = mobileNumber
            setOtpData(it.otp)
            _successfulUserCheckLiveData.postValue(true)
        }.onError {
            if (it.responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                _duplicateUserLiveData.postValue(true)
            } else {
                errorLiveData.postValue(it)
            }
        }
    }

    fun resendOtp() = viewModelScope.launch {
        val response = processCoroutine({ repository.sendOtp(SendOtpRequest(mobileNumberLiveData.value!!)) })
        response.onSuccess {
            setOtpData(it.otp)
            _successfulOtpResendLiveData.postValue(true)
        }.onError {
            errorLiveData.postValue(it)
        }
    }

    fun verifyOtpAndLogin(otp: String) {
        if (DateTimeUtils.getDifferenceInMinutes(otpLiveData.value!!.second) > Constants.USER_OTP_EXPIRY_TIME_IN_MINUTES) {
            _otpExpiredLiveData.postValue(true)
        } else if (otp != otpLiveData.value!!.first) {
            _invalidOtpLiveData.postValue(true)
        } else {
            _profileCreationRequiredLiveData.postValue(true)
        }
    }

    fun createUser(request: AddUserDetailsRequest) = viewModelScope.launch {
        val response = repository.createUser(request)
        response.onSuccess {
            _successfulUserAdditionLiveData.postValue(true)
        }.onError {
            errorLiveData.postValue(it)
        }
    }

    fun setInvalidMobileNumberLiveData(isInvalid: Boolean) {
        _invalidMobileNumberLiveData.postValue(isInvalid)
    }

    private fun setOtpData(otp: String) {
        savedStateHandle[OTP] = Pair(otp, System.currentTimeMillis())
    }
}