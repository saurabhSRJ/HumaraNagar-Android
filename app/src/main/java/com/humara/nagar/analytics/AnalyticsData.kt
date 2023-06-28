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
            const val MAIN_ACTIVITY = "MAIN_ACTIVITY"
            const val HOME_FRAGMENT = "HOME_FRAGMENT"
            const val REPORT_FRAGMENT = "REPORT_FRAGMENT"
            const val SETTINGS_FRAGMENT = "SETTINGS_FRAGMENT"
            const val RESIDENTS_FRAGMENT = "RESIDENTS_FRAGMENT"
            const val PENDING_APPROVAL_FRAGMENT = "PENDING_APPROVAL_FRAGMENT"
            const val COMPLAINTS_FRAGMENT = "COMPLAINTS_FRAGMENT"
            const val COMPLAINTS_STATUS_FRAGMENT = "COMPLAINTS_STATUS_FRAGMENT"
            const val IMAGE_PREVIEW_FRAGMENT = "IMAGE_PREVIEW_FRAGMENT"
            const val WEB_VIEW_ACTIVITY = "WEB_VIEW_ACTIVITY"
            const val LOGOUT_BOTTOM_SHEET = "LOGOUT_BOTTOM_SHEET"
            const val POST_DETAILS_FRAGMENT = "POST_DETAILS_FRAGMENT"
            const val CREATE_POST_FRAGMENT = "CREATE_POST_FRAGMENT"
            const val CREATE_POLL_FRAGMENT = "CREATE_POLL_FRAGMENT"
            const val ADD_PROFILE_PHOTO_FRAGMENT = "ADD_PROFILE_PHOTO_FRAGMENT"
            const val MEDIA_SELECTION_BOTTOM_SHEET = "MEDIA_SELECTION_BOTTOM_SHEET"
            const val VIDEO_PLAYER_FRAGMENT = "VIDEO_PLAYER_FRAGMENT"
            const val ADD_USER_MOBILE_VERIFICATION_FRAGMENT = "ADD_USER_MOBILE_VERIFICATION_FRAGMENT"
            const val ADD_USER_DETAILS_FRAGMENT = "ADD_USER_DETAILS_FRAGMENT"
        }
    }

    interface EventName {
        companion object {
            const val LOGOUT = "LOGOUT"
            const val NOTIFICATION_RECEIVED = "NOTIFICATION_RECEIVED"
            const val NOTIFICATION_DISMISSED = "NOTIFICATION_DISMISSED"
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
            const val NOTIFICATION_DATA = "NOTIFICATION_DATA"
            const val ROLE = "ROLE"
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