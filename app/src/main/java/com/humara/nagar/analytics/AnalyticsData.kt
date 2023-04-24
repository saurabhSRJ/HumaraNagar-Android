package com.humara.nagar.analytics

import com.humara.nagar.constants.IntentKeyConstants

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
            const val PENDING_APPROVAL_FRAGMENT = "PENDING_APPROVAL_FRAGMENT"
        }
    }

    interface EventName {
        companion object {
            const val UNAUTHORIZED_ACCESS = "UNAUTHORIZED_ACCESS"
            const val LOGOUT = "LOGOUT"
        }
    }

    interface Parameters {
        companion object {
            const val EVENT_TYPE = "EVENT_TYPE"
            const val SOURCE = IntentKeyConstants.SOURCE
            const val LANGUAGE_CODE = "LANGUAGE_CODE"
            const val PAGE_TYPE = "PAGE_TYPE"
            const val USER_ID = "USER_ID"
            const val USER_NAME = "USER_NAME"
            const val MOBILE_NUMBER = "MOBILE_NUMBER"
        }
    }

    interface EventType {
        companion object {
            const val BUTTON_CLICK = "BUTTON_CLICK"
            const val SCREEN_VIEW = "SCREEN_VIEW"
            const val PAGE_LOAD = "PAGE_LOAD"
        }
    }
}