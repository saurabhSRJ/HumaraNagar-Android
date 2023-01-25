package com.humara.nagar.constants

/**
 * Generic App Constants
 */
interface Constants {
    companion object {
        const val ENGLISH = "en"
        const val HINDI = "hi"
        const val PREF_DEFAULT_IN_MEMORY_CACHE_SIZE = 15
        const val KEY_APP_PREFERENCE = "com.humara.nagar.APP_INFO_PREF"
        const val USER_PREFERENCE_KEY = "com.humara.nagar.USER_INFO_PREF"
        const val COUNTRY_CODE = "+91"
        const val OTP_RESEND_TIMER_IN_MS = 10*1000L
    }
}