package com.humara.nagar.constants

interface NetworkConstants {
    interface NetworkHeaderConstants {
        companion object {
            const val X_IMEI_NUMBER = "x-imei-number"
            const val ACCEPT_LANGUAGE = "Accept-Language"
            const val X_FORWARDED_FOR = "X-Forwarded-For"
            const val APP_VERSION = "App-Version"
            const val ANDROID_VERSION = "Android-Version"
            const val X_SOURCE_ID = "X-SOURCE-ID"
            const val X_DEVICE_ID = "x-device-id"
        }
    }

    interface NetworkAPIConstants {
        companion object {
            const val OTP = "/otp"
            const val LOGIN = "/login"
            const val SIGNUP = "/signup"
            const val CONFIG = "/config"
        }
    }

    interface NetworkQueryConstants {
        companion object {

        }
    }
}