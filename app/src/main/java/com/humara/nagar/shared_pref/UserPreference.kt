package com.humara.nagar.shared_pref

import android.content.Context
import com.humara.nagar.constants.Constants
import com.humara.nagar.constants.SharedPreferenceKeys
import com.humara.nagar.ui.signup.model.User

/**
 * encrypted shared preference class for user-session related data
 */
class UserPreference(context: Context) : EncryptedSharedPreference(context, Constants.USER_PREFERENCE_KEY) {
    // The inline modifier can be used on accessors of properties that don't have backing fields
    inline var mobileNumber: String
        get() = getString(SharedPreferenceKeys.UserPreferenceKeys.MOBILE_NUMBER, "") ?: ""
        set(value) = putString(SharedPreferenceKeys.UserPreferenceKeys.MOBILE_NUMBER, value)

    inline var userProfile: User?
        get() = getObject(SharedPreferenceKeys.UserPreferenceKeys.USER_PROFILE, User::class.java)
        set(value) = putObject(SharedPreferenceKeys.UserPreferenceKeys.USER_PROFILE, value)

    inline val isUserLoggedIn: Boolean
        get() = false
//        get() = (userProfile != null)
}