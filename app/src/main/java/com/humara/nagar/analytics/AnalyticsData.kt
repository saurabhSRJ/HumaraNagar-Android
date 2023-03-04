package com.humara.nagar.analytics

interface AnalyticsData {
    interface ScreenName {
        companion object {
            const val SPLASH_ACTIVITY = "SPLASH_ACTIVITY"
            const val ONBOARD_ACTIVITY = "ONBOARD_ACTIVITY"
            const val SIGNUP_OR_LOGIN_FRAGMENT = "SIGNUP_OR_LOGIN_FRAGMENT"
            const val OTP_VERIFICATION_FRAGMENT = "OTP_VERIFICATION_FRAGMENT"
            const val PROFILE_CREATION_FRAGMENT = "PROFILE_CREATION_FRAGMENT"
            const val FORCE_LOGOUT_SCREEN = "FORCE_LOGOUT_SCREEN"
            const val MAIN_ACTIVITY = "MAIN_ACTIVITY"
            const val HOME_FRAGMENT = "HOME_FRAGMENT"
            const val REPORT_FRAGMENT = "REPORT_FRAGMENT"
            const val CERTIFICATES_FRAGMENT = "CERTIFICATES_FRAGMENT"
            const val RESIDENTS_FRAGMENT = "RESIDENTS_FRAGMENT"
            const val COMPLAINTS_FRAGMENT = "COMPLAINTS_FRAGMENT"
            const val COMPLAINTS_STATUS_FRAGMENT = "COMPLAINTS_STATUS_FRAGMENT"
            const val IMAGE_PREVIEW_FRAGMENT = "IMAGE_PREVIEW_FRAGMENT"
        }
    }

    interface EventName {
        companion object {
            const val UNAUTHORIZED_ACCESS = "UNAUTHORIZED_ACCESS"
        }
    }
}