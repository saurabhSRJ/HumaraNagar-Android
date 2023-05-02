package com.humara.nagar.shared_pref

import android.content.Context
import com.humara.nagar.constants.Constants
import com.humara.nagar.constants.SharedPreferenceKeys
import com.humara.nagar.ui.signup.model.Role
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

    inline var historyToolTipCounter: Int
        get() = getInt(SharedPreferenceKeys.UserPreferenceKeys.HISTORY_TOOLTIP_COUNTER, 0)
        set(value) = putInt(SharedPreferenceKeys.UserPreferenceKeys.HISTORY_TOOLTIP_COUNTER, value)

    inline var isUserLoggedIn: Boolean
        get() = getBoolean(SharedPreferenceKeys.UserPreferenceKeys.USER_LOGGED_IN, false)
        set(value) = putBoolean(SharedPreferenceKeys.UserPreferenceKeys.USER_LOGGED_IN, value)

    inline var passCode: String
        get() = getString(SharedPreferenceKeys.UserPreferenceKeys.PASSCODE, "") ?: ""
        set(value) = putString(SharedPreferenceKeys.UserPreferenceKeys.PASSCODE, value)

    inline var token: String
        get() = getString(SharedPreferenceKeys.UserPreferenceKeys.TOKEN, "") ?: ""
        set(value) = putString(SharedPreferenceKeys.UserPreferenceKeys.TOKEN, value)

    inline var refreshToken: String
        get() = getString(SharedPreferenceKeys.UserPreferenceKeys.REFRESH_TOKEN, "") ?: ""
        set(value) = putString(SharedPreferenceKeys.UserPreferenceKeys.REFRESH_TOKEN, value)

    inline var role: String?
        get() = getString(SharedPreferenceKeys.UserPreferenceKeys.ROLE, null)
        set(value) = putString(SharedPreferenceKeys.UserPreferenceKeys.ROLE, value)

    val isAdminUser: Boolean = role == Role.ADMIN.role

    inline var fcmToken: String?
        get() = getString(SharedPreferenceKeys.UserPreferenceKeys.FCM_TOKEN, null)
        set(value) = putString(SharedPreferenceKeys.UserPreferenceKeys.FCM_TOKEN, value)
}