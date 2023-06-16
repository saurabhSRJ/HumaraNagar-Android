package com.humara.nagar.ui.signup.profile_creation

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.ui.signup.model.Gender
import com.humara.nagar.ui.signup.profile_creation.model.ProfileCreationRequest
import com.humara.nagar.utils.DateTimeUtils
import com.humara.nagar.utils.SingleLiveEvent
import com.humara.nagar.utils.StringUtils
import com.humara.nagar.utils.UserDataValidator

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

    fun getProfileCreationObjectWithCollectedData(): ProfileCreationRequest {
        return ProfileCreationRequest(
            userId = getUserPreference().userId,
            name = StringUtils.replaceWhitespaces(getUserName()!!.trim()),
            fatherOrSpouseName = StringUtils.replaceWhitespaces(getParentName()!!.trim()),
            dateOfBirth = DateTimeUtils.
            convertDateFormat(getDateOfBirth().value.toString(), "dd-MM-yyyy", "yyyy-MM-dd"),
            gender = getGender(),
            locality = getLocality()!!
        )
    }

    private fun updateSubmitButtonState() {
        val anyRequiredFieldEmpty = getUserName().isNullOrEmpty() || getDateOfBirth().value.isNullOrEmpty() || getParentName().isNullOrEmpty() || getLocality().isNullOrEmpty()
        savedStateHandle[SUBMIT_BUTTON_KEY] = anyRequiredFieldEmpty.not()
    }

    fun getSubmitButtonState(): LiveData<Boolean> = savedStateHandle.getLiveData(SUBMIT_BUTTON_KEY, false)
}