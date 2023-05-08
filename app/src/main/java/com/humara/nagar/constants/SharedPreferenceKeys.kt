package com.humara.nagar.constants

import com.humara.nagar.shared_pref.AppPreference
import com.humara.nagar.shared_pref.UserPreference

/**
 * class containing @see[AppPreference] and @see[UserPreference] keys
 */
interface SharedPreferenceKeys {

    /**
     * Preference keys holds user session related data
     */
    interface UserPreferenceKeys {
        companion object {
            const val MOBILE_NUMBER = "MOBILE_NUMBER"
            const val USER_PROFILE = "USER_PROFILE"
            const val HISTORY_TOOLTIP_COUNTER = "HISTORY_TOOLTIP_COUNTER"
            const val PASSCODE = "PASSCODE"
            const val TOKEN = "TOKEN"
            const val REFRESH_TOKEN = "REFRESH_TOKEN"
            const val USER_LOGGED_IN = "USER_LOGGED_IN"
            const val ROLE = "ROLE"
            const val FCM_TOKEN = "FCM_TOKEN"
            const val FCM_TOKE_UPDATED = "FCM_TOKE_UPDATED"
        }
    }

    /**
     * Preference keys holding app related data
     */
    interface AppPreferenceKeys {
        companion object {
            const val APP_LANGUAGE = "APP_LANGUAGE"
        }
    }
}