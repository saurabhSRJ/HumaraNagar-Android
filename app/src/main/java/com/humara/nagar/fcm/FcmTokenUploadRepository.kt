package com.humara.nagar.fcm

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.humara.nagar.Logger
import com.humara.nagar.constants.SharedPreferenceKeys
import com.humara.nagar.network.BaseRepository
import com.humara.nagar.utils.getUserSharedPreferences

class FcmTokenUploadRepository(context: Context) : BaseRepository(context) {
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
            with(context.getUserSharedPreferences()) {
                //TODO: remove fcm token from the backend
                remove(SharedPreferenceKeys.UserPreferenceKeys.FCM_TOKEN)
            }
        }
    }

    /**
     * Replace new token with existing token if both are not same
     */
    fun resetExistingTokenIfNotSameAsNew(newToken: String?) {
        val oldToken = context.getUserSharedPreferences().fcmToken
        if (newToken == null || oldToken == newToken) {
            return
        }
        Logger.debugLog("FirebaseInstanceId token: $newToken")
        context.getUserSharedPreferences().fcmToken = newToken
        //TODO: upload fcm token to the backend
    }
}