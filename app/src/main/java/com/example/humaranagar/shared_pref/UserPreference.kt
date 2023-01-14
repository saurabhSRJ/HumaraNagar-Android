package com.example.humaranagar.shared_pref

import android.content.Context
import com.example.humaranagar.constants.Constants
import com.example.humaranagar.constants.SharedPreferenceKeys

class UserPreference(context: Context): EncryptedSharedPreference(context, Constants.USER_PREFERENCE_KEY) {
    fun getMobileNumber(): String = getString(SharedPreferenceKeys.UserPreferenceKeys.MOBILE_NUMBER, "") ?: ""

    fun setMobileNumber(mobileNumber: String) = putString(SharedPreferenceKeys.UserPreferenceKeys.MOBILE_NUMBER, mobileNumber)
}