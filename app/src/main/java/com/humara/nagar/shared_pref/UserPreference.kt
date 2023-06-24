package com.humara.nagar.shared_pref

import android.content.Context
import com.humara.nagar.constants.Constants
import com.humara.nagar.constants.SharedPreferenceKeys
import com.humara.nagar.ui.signup.model.RoleDetails
import com.humara.nagar.ui.signup.model.User

/**
 * encrypted shared preference class for user-session related data
 */
class UserPreference(context: Context) : EncryptedSharedPreference(context, Constants.USER_PREFERENCE_KEY) {
    // The inline modifier can be used on accessors of properties that don't have backing fields
    inline var mobileNumber: String
        get() = getString(SharedPreferenceKeys.UserPreferenceKeys.MOBILE_NUMBER, "") ?: ""
        set(value) = putString(SharedPreferenceKeys.UserPreferenceKeys.MOBILE_NUMBER, value)

    inline var userId: Long
        get() = getLong(SharedPreferenceKeys.UserPreferenceKeys.USER_ID, 0L)
        set(value) = putLong(SharedPreferenceKeys.UserPreferenceKeys.USER_ID, value)

    inline var userName: String?
        get() = getString(SharedPreferenceKeys.UserPreferenceKeys.USER_NAME, "")
        set(value) = putString(SharedPreferenceKeys.UserPreferenceKeys.USER_NAME, value)

    inline var role: RoleDetails?
        get() = getObject(SharedPreferenceKeys.UserPreferenceKeys.ROLE, RoleDetails::class.java)
        set(value) = putObject(SharedPreferenceKeys.UserPreferenceKeys.ROLE, value)

    inline var isAdminUser: Boolean
        get() = getBoolean(SharedPreferenceKeys.UserPreferenceKeys.ADMIN_USER, false)
        set(value) = putBoolean(SharedPreferenceKeys.UserPreferenceKeys.ADMIN_USER, value)

    inline var ward: String?
        get() = getString(SharedPreferenceKeys.UserPreferenceKeys.WARD, null)
        set(value) = putString(SharedPreferenceKeys.UserPreferenceKeys.WARD, value)

    inline var userProfile: User?
        get() = getObject(SharedPreferenceKeys.UserPreferenceKeys.USER_PROFILE, User::class.java)
        set(value) = putObject(SharedPreferenceKeys.UserPreferenceKeys.USER_PROFILE, value)

    inline var profileImage: String?
        get() = getString(SharedPreferenceKeys.UserPreferenceKeys.PROFILE_IMAGE, null)
        set(value) = putString(SharedPreferenceKeys.UserPreferenceKeys.PROFILE_IMAGE, value)

    inline var isUserLoggedIn: Boolean
        get() = getBoolean(SharedPreferenceKeys.UserPreferenceKeys.USER_LOGGED_IN, false)
        set(value) = putBoolean(SharedPreferenceKeys.UserPreferenceKeys.USER_LOGGED_IN, value)

    inline var passCode: String
        get() = getString(SharedPreferenceKeys.UserPreferenceKeys.PASSCODE, "") ?: ""
        set(value) = putString(SharedPreferenceKeys.UserPreferenceKeys.PASSCODE, value)

    inline var token: String?
        get() = getString(SharedPreferenceKeys.UserPreferenceKeys.TOKEN, null)
        set(value) = putString(SharedPreferenceKeys.UserPreferenceKeys.TOKEN, value)

    inline var refreshToken: String
        get() = getString(SharedPreferenceKeys.UserPreferenceKeys.REFRESH_TOKEN, "") ?: ""
        set(value) = putString(SharedPreferenceKeys.UserPreferenceKeys.REFRESH_TOKEN, value)

    inline var fcmToken: String?
        get() = getString(SharedPreferenceKeys.UserPreferenceKeys.FCM_TOKEN, null)
        set(value) = putString(SharedPreferenceKeys.UserPreferenceKeys.FCM_TOKEN, value)

    inline var fcmTokenUpdated: Boolean
        get() = getBoolean(SharedPreferenceKeys.UserPreferenceKeys.FCM_TOKE_UPDATED, false)
        set(value) = putBoolean(SharedPreferenceKeys.UserPreferenceKeys.FCM_TOKE_UPDATED, value)

    inline var historyToolTipCounter: Int
        get() = getInt(SharedPreferenceKeys.UserPreferenceKeys.HISTORY_TOOLTIP_COUNTER, 0)
        set(value) = putInt(SharedPreferenceKeys.UserPreferenceKeys.HISTORY_TOOLTIP_COUNTER, value)
}