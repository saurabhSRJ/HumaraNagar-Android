package com.humara.nagar.ui.user_profile

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.network.onError
import com.humara.nagar.network.onSuccess
import com.humara.nagar.ui.user_profile.model.UpdateUserProfileRequest
import com.humara.nagar.ui.user_profile.model.UserProfile
import com.humara.nagar.utils.DateTimeUtils
import com.humara.nagar.utils.SingleLiveEvent
import com.humara.nagar.utils.UserDataValidator
import kotlinx.coroutines.launch

class UserProfileViewModel(application: Application, val savedStateHandle: SavedStateHandle) : BaseViewModel(application) {
    companion object {
        private const val BIO = "bio"
        private const val PARENT_NAME = "parent"
        private const val DOB = "dob"
        private const val EMAIL = "email"
    }

    private val repository = UserProfileRepository(application)
    private val _userProfileLiveData: MutableLiveData<UserProfile> by lazy { MutableLiveData() }
    val userProfileLiveData: LiveData<UserProfile> = _userProfileLiveData
    private val _invalidEmailLiveData: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }
    val invalidEmailLiveData: LiveData<Boolean> = _invalidEmailLiveData
    private val _invalidDateOfBirthLiveData: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }
    val invalidDateOfBirthLiveData: LiveData<Boolean> = _invalidDateOfBirthLiveData
    private val _updateProfileSuccessLiveData: SingleLiveEvent<UserProfile> by lazy { SingleLiveEvent() }
    val updateProfileSuccessLiveData: LiveData<UserProfile> = _updateProfileSuccessLiveData
    private val bioLiveData: LiveData<String> = savedStateHandle.getLiveData(BIO)
    private val parentNameLiveData: LiveData<String> = savedStateHandle.getLiveData(PARENT_NAME)
    val dobLiveData: LiveData<String> = savedStateHandle.getLiveData(DOB)
    private val emailLiveData: LiveData<String> = savedStateHandle.getLiveData(EMAIL)

    init {
        getUserProfile()
    }

    private fun getUserProfile() = viewModelScope.launch {
        val response = processCoroutine({ repository.getUserProfile(getUserPreference().userId) })
        response.onSuccess {
            _userProfileLiveData.postValue(it.userProfile)
        }.onError {
            errorLiveData.postValue(it)
        }
    }

    fun updateProfile() = viewModelScope.launch {
        val shouldCheckEmailValidity = emailLiveData.value.isNullOrEmpty().not()
        if (shouldCheckEmailValidity && UserDataValidator.isValidEmail(emailLiveData.value).not()) {
            _invalidEmailLiveData.postValue(true)
            return@launch
        }
        val request = UpdateUserProfileRequest(
            bio = bioLiveData.value,
            fatherSpouseName = parentNameLiveData.value.toString(),
            dateOfBirth = DateTimeUtils.convertDateFormat(dobLiveData.value.toString(), "dd-MM-yyyy", "yyyy-MM-dd"),
            email = emailLiveData.value
        )
        val response = processCoroutine({ repository.updateUserProfile(getUserPreference().userId, request) })
        response.onSuccess {
            _updateProfileSuccessLiveData.postValue(it.userProfile)
        }
    }

    fun setBio(bio: String) {
        savedStateHandle[BIO] = bio
    }

    fun setParentName(name: String) {
        savedStateHandle[PARENT_NAME] = name
    }

    fun setDob(dob: String) {
        if (UserDataValidator.isValidDateOfBirth(dob)) {
            savedStateHandle[DOB] = dob
        } else {
            _invalidDateOfBirthLiveData.value = true
        }
    }

    fun setEmail(email: String) {
        savedStateHandle[EMAIL] = email
    }
}