package com.humara.nagar.constants

interface NetworkConstants {
    interface NetworkHeaderConstants {
        companion object {
            const val ACCEPT_LANGUAGE = "Accept-Language"
            const val APP_VERSION = "App-Version"
            const val ANDROID_VERSION = "Android-Version"
        }
    }

    interface NetworkAPIConstants {
        companion object {
            const val OTP = "/otp"
            const val LOGIN = "/login"
            const val SIGNUP = "/signup"
            const val CONFIG = "/config"
            const val LOGOUT = "/logout"
            const val COMPLAINT = "/complaint"
        }
    }

    interface NetworkQueryConstants {
        companion object {
            const val ID = "id"
        }
    }
}