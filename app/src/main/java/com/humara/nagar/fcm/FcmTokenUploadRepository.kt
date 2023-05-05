package com.humara.nagar.fcm

import android.app.Application
import com.google.firebase.messaging.FirebaseMessaging
import com.humara.nagar.Logger
import com.humara.nagar.constants.SharedPreferenceKeys
import com.humara.nagar.network.BaseRepository
import com.humara.nagar.utils.getUserSharedPreferences

class FcmTokenUploadRepository(application: Application) : BaseRepository(application) {
    /**
     * Get the push token using Firebase SDK and replace with existing token
     * when the user sign in/ sign up
     */
    fun fetchFcmTokenAndResetIfRequired() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            resetExistingTokenIfNotSameAsNew(token)
        }
    }

    fun removeFcmTokenOnLogout() {
        FirebaseMessaging.getInstance().deleteToken().addOnSuccessListener {
            with(application.getUserSharedPreferences()) {
                //TODO: remove fcm token from the backend
                remove(SharedPreferenceKeys.UserPreferenceKeys.FCM_TOKEN)
            }
        }
    }

    /**
     * Replace new token with existing token if both are not same
     */
    fun resetExistingTokenIfNotSameAsNew(newToken: String?) {
        val oldToken = application.getUserSharedPreferences().fcmToken
        if (newToken == null || oldToken == newToken) {
            return
        }
        Logger.debugLog("FirebaseInstanceId token: $newToken")
        application.getUserSharedPreferences().fcmToken = newToken
        //TODO: upload fcm token to the backend
    }
}