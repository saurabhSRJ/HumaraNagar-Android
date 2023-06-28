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
        const val OTP_RESEND_TIMER_IN_MS = 60 * 1000L
        const val USER_OTP_EXPIRY_TIME_IN_MINUTES = 5
        const val MIN_AGE_REQUIREMENT = 18L
        const val MAX_DOCUMENT_SIZE_IN_BYTES = 4 * 1024 * 1024
        const val MAX_DOCUMENT_SIZE_IN_MB = MAX_DOCUMENT_SIZE_IN_BYTES / (1024 * 1024)
        const val MAX_VIDEO_SIZE_IN_BYTES = 10 * 1024 * 1024
        const val MAX_VIDEO_SIZE_IN_MB = MAX_VIDEO_SIZE_IN_BYTES / (1024 * 1024)
    }
}