package com.humara.nagar.ui.signup.profile_creation

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.ui.signup.model.Gender
import com.humara.nagar.ui.signup.model.User
import com.humara.nagar.utils.UserDataValidator

class ProfileCreationViewModel(application: Application): BaseViewModel(application) {
    private val _dateOfBirthLiveData: MutableLiveData<String> by lazy { MutableLiveData() }
    val dateOfBirthLiveData: LiveData<String> = _dateOfBirthLiveData
    private val _invalidDateOfBirthLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val invalidDateOfBirthLiveData: LiveData<Boolean> = _invalidDateOfBirthLiveData
    private val _userNameLiveData: MutableLiveData<String> by lazy { MutableLiveData() }
    private val _enableSubmitButtonLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val enableSubmitButtonLiveData: LiveData<Boolean> = _enableSubmitButtonLiveData
    private val _parentNameLiveData: MutableLiveData<String> by lazy { MutableLiveData() }
    private val _localityLiveData: MutableLiveData<String> by lazy { MutableLiveData() }
    private val _genderLiveData: MutableLiveData<String> by lazy { MutableLiveData() }

    fun setDateOfBirth(dob: String) {
        if (UserDataValidator.isValidDateOfBirth(dob)) {
            _dateOfBirthLiveData.value = dob
        } else {
            _invalidDateOfBirthLiveData.value = true
        }
        updateSubmitButtonState()
    }

    fun setUserName(name: String) {
        _userNameLiveData.value = name
        updateSubmitButtonState()
    }

    fun setParentName(name: String) {
        _parentNameLiveData.value = name
        updateSubmitButtonState()
    }

    fun setLocality(locality: String) {
        _localityLiveData.value = locality
        updateSubmitButtonState()
    }

    fun setGender(gender: String) {
        _genderLiveData.value = gender
    }

    fun getUserObjectWithCollectedData(): User {
        val user: User = getUserPreference().userProfile!!.apply {
            name = _userNameLiveData.value.toString()
            fatherOrSpouseName = _parentNameLiveData.value.toString()
            dateOfBirth = _dateOfBirthLiveData.value.toString()
            locality = _localityLiveData.value.toString()
            gender = _genderLiveData.value ?: Gender.MALE.name
        }
        return user
    }

    private fun updateSubmitButtonState() {
        val anyRequiredFieldEmpty = _userNameLiveData.value.isNullOrEmpty() || _dateOfBirthLiveData.value.isNullOrEmpty() || _parentNameLiveData.value.isNullOrEmpty() || _localityLiveData.value.isNullOrEmpty()
        _enableSubmitButtonLiveData.value = anyRequiredFieldEmpty.not()
    }
}