package com.example.humaranagar.constants

import com.example.humaranagar.shared_pref.AppPreference
import com.example.humaranagar.shared_pref.UserPreference

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
        }
    }

    /**
     * Preference keys holding app related data
     */
    interface AppPreferenceKeys {
        companion object {

        }
    }
}