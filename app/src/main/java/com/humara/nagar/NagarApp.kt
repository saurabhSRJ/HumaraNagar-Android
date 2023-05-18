package com.humara.nagar

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.analytics.AnalyticsTracker
import com.humara.nagar.shared_pref.AppPreference
import com.humara.nagar.shared_pref.UserPreference

class NagarApp : Application() {
    companion object {
        const val TAG = "NagarApp"
    }

    //property is visible everywhere but setter is private
    lateinit var appSharedPreference: AppPreference
        private set
    lateinit var userSharedPreference: UserPreference
        private set

    override fun onCreate() {
        super.onCreate()
        appSharedPreference = initializeAppPreference()
        userSharedPreference = initializeUserPreference()
        initFirebaseCrashlyticsProps()
        AnalyticsTracker.getInstance()
    }

    private fun initializeAppPreference(): AppPreference {
        return AppPreference(this)
    }

    private fun initializeUserPreference(): UserPreference {
        return UserPreference(this)
    }

    private fun initFirebaseCrashlyticsProps() {
        userSharedPreference.userProfile?.let { user ->
            FirebaseCrashlytics.getInstance().apply {
                setCustomKey(AnalyticsData.Parameters.USER_NAME, user.name)
                setUserId(user.userId.toString())
                setCustomKey(AnalyticsData.Parameters.MOBILE_NUMBER, user.mobileNumber)
            }
        }
    }
}