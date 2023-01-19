package com.example.humaranagar

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.example.humaranagar.shared_pref.AppPreference
import com.example.humaranagar.shared_pref.UserPreference

class NagarApp : Application(), Application.ActivityLifecycleCallbacks {
    companion object {
        const val TAG = "NagarApp"
    }

    //property is visible everywhere but setter is private
    lateinit var appSharedPreference: AppPreference
        private set
    lateinit var userSharedPreference: UserPreference
        private set
    private var currentActivity: Activity? = null

    override fun onCreate() {
        super.onCreate()
        appSharedPreference = initializeAppPreference()
        userSharedPreference = initializeUserPreference()
    }

    private fun initializeAppPreference(): AppPreference {
        return AppPreference(this)
    }

    private fun initializeUserPreference(): UserPreference {
        return UserPreference(this)
    }

    fun logout() {
        appSharedPreference.logOut(false)
        finishCurrentTopActivity()
    }

    private fun finishCurrentTopActivity() {
        try {
            currentActivity?.finish()
            currentActivity = null
        } catch (ex: Exception) {
            Logger.logException(TAG, ex, Logger.LogLevel.ERROR, true)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        currentActivity = activity
    }

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}
}