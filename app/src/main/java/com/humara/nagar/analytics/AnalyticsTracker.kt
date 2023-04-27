package com.humara.nagar.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.humara.nagar.Logger
import com.humara.nagar.NagarApp
import com.humara.nagar.shared_pref.UserPreference
import org.json.JSONObject

class AnalyticsTracker private constructor() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    init {
        initializeFirebaseAnalyticsIfNotInitializedAlready()
    }

    private fun initializeFirebaseAnalyticsIfNotInitializedAlready() {
        if (this::firebaseAnalytics.isInitialized.not()) firebaseAnalytics = Firebase.analytics
    }

    companion object {
        const val TAG = "AnalyticsTracker"

        @Volatile
        private var INSTANCE: AnalyticsTracker? = null

        fun getInstance(): AnalyticsTracker = INSTANCE ?: synchronized(this) {
            INSTANCE ?: AnalyticsTracker().also {
                INSTANCE = it
            }
        }

        fun sendEvent(event: String, properties: JSONObject?) {
            INSTANCE?.let {
                Logger.debugLog(TAG, "$event---------$properties")
                it.logEventToFirebase(event, properties)
            }
        }

        fun onUserOnBoard(context: Context) {
            val userPreference: UserPreference = (context as NagarApp).userSharedPreference
            userPreference.userProfile?.let { user ->
                INSTANCE?.run {
                    firebaseAnalytics.setUserProperty(AnalyticsData.Parameters.USER_ID, user.userId.toString())
                    firebaseAnalytics.setUserProperty(AnalyticsData.Parameters.USER_NAME, user.name)
                    firebaseAnalytics.setUserProperty(AnalyticsData.Parameters.MOBILE_NUMBER, user.mobileNumber)
                }
            }
        }
    }

    private fun logEventToFirebase(event: String, properties: JSONObject?) {
        initializeFirebaseAnalyticsIfNotInitializedAlready()
        firebaseAnalytics.logEvent(event, getBundleObjectForAnalyticsEvents(properties))
    }

    private fun getBundleObjectForAnalyticsEvents(
        properties: JSONObject?,
    ): Bundle {
        val bundle = Bundle()
        properties?.let {
            val iterator = properties.keys()
            while (iterator.hasNext()) {
                var key = iterator.next()
                try {
                    val value = properties[key]
                    key =
                        getStringValueWhereSpaceIsReplacedByUnderScore(key) // Modifying the Key value as Firebase doesn't accept space separator
                    when (value) {
                        is Int -> {
                            bundle.putInt(key, value)
                        }
                        is Double -> {
                            bundle.putDouble(key, value)
                        }
                        is Long -> {
                            bundle.putLong(key, value)
                        }
                        is Float -> {
                            bundle.putFloat(key, value)
                        }
                        else -> {
                            bundle.putString(key, value.toString())
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.message!!)
                }
            }
        }
        return bundle
    }

    private fun getStringValueWhereSpaceIsReplacedByUnderScore(valueToOperate: String): String? {
        return valueToOperate.replace("( - )|[ -]".toRegex(), "_")
    }
}