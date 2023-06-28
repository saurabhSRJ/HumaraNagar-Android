package com.humara.nagar.ui.signup.profile_creation

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.ui.add_user.model.AddUserDetailsRequest
import com.humara.nagar.ui.signup.model.GenderDetails
import com.humara.nagar.ui.signup.model.RoleDetails
import com.humara.nagar.ui.signup.model.WardDetails
import com.humara.nagar.ui.signup.profile_creation.model.ProfileCreationRequest
import com.humara.nagar.utils.DateTimeUtils
import com.humara.nagar.utils.SingleLiveEvent
import com.humara.nagar.utils.StringUtils.convertToLowerCase
import com.humara.nagar.utils.UserDataValidator

class ProfileCreationViewModel(application: Application, private val savedStateHandle: SavedStateHandle) : BaseViewModel(application) {
    companion object {
        private const val USERNAME_KEY = "username"
        private const val PARENT_NAME_KEY = "parent"
        private const val DOB_KEY = "dob"
        private const val GENDER_KEY = "gender"
        private const val WARD_KEY = "ward"
        private const val ROLE_KEY = "role"
        private const val SUBMIT_BUTTON_KEY = "submit"
        private const val ADD_USER_BUTTON_KEY = "add_user"
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
        updateAddUserButtonState()
    }

    fun getDateOfBirth(): LiveData<String> = savedStateHandle.getLiveData(DOB_KEY)

    fun setUserName(name: String) {
        savedStateHandle[USERNAME_KEY] = name
        updateSubmitButtonState()
        updateAddUserButtonState()
    }

    private fun getUserName(): String? = savedStateHandle[USERNAME_KEY]

    fun setParentName(name: String) {
        savedStateHandle[PARENT_NAME_KEY] = name
        updateSubmitButtonState()
        updateAddUserButtonState()
    }

    private fun getParentName(): String? = savedStateHandle[PARENT_NAME_KEY]

    fun setWard(ward: WardDetails) {
        savedStateHandle[WARD_KEY] = ward
        updateSubmitButtonState()
        updateAddUserButtonState()
    }

    private fun getWard(): WardDetails? = savedStateHandle[WARD_KEY]

    fun setGender(gender: GenderDetails) {
        savedStateHandle[GENDER_KEY] = gender
        updateSubmitButtonState()
        updateAddUserButtonState()
    }

    private fun getGender(): GenderDetails? = savedStateHandle[GENDER_KEY]

    fun setRole(role: RoleDetails) {
        savedStateHandle[ROLE_KEY] = role
        updateAddUserButtonState()
    }

    private fun getRole(): RoleDetails? = savedStateHandle[ROLE_KEY]

    fun getProfileCreationObjectWithCollectedData(): ProfileCreationRequest {
        return ProfileCreationRequest(
            userId = getUserPreference().userId,
            name = getUserName()?.convertToLowerCase() ?: "",
            fatherOrSpouseName = getParentName()?.convertToLowerCase() ?: "",
            dateOfBirth = DateTimeUtils.convertDateFormat(getDateOfBirth().value.toString(), "dd-MM-yyyy", "yyyy-MM-dd"),
            genderId = getGender()!!.id,
            wardId = getWard()!!.id
        )
    }

    fun getAddUserDetailsObjectWithCollectedData(mobileNumber: String): AddUserDetailsRequest {
        return AddUserDetailsRequest(
            roleId = getRole()!!.id,
            name = getUserName()?.convertToLowerCase() ?: "",
            fatherOrSpouseName = getParentName()?.convertToLowerCase() ?: "",
            dateOfBirth = DateTimeUtils.convertDateFormat(getDateOfBirth().value.toString(), "dd-MM-yyyy", "yyyy-MM-dd"),
            genderId = getGender()!!.id,
            wardId = getWard()!!.id,
            mobileNumber = mobileNumber
        )
    }

    private fun updateSubmitButtonState() {
        val anyRequiredFieldEmpty = getUserName().isNullOrEmpty() || getDateOfBirth().value.isNullOrEmpty() || getParentName().isNullOrEmpty() || getWard() == null || getGender() == null
        savedStateHandle[SUBMIT_BUTTON_KEY] = anyRequiredFieldEmpty.not()
    }

    private fun updateAddUserButtonState() {
        val anyRequiredFieldEmpty = getUserName().isNullOrEmpty() || getDateOfBirth().value.isNullOrEmpty() || getParentName().isNullOrEmpty() || getWard() == null ||
                getGender() == null || getRole() == null
        savedStateHandle[ADD_USER_BUTTON_KEY] = anyRequiredFieldEmpty.not()
    }

    fun getSubmitButtonState(): LiveData<Boolean> = savedStateHandle.getLiveData(SUBMIT_BUTTON_KEY, false)

    fun getAddUserButtonState(): LiveData<Boolean> = savedStateHandle.getLiveData(ADD_USER_BUTTON_KEY, false)
}