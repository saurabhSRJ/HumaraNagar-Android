package com.humara.nagar

import android.app.Application
import android.content.Context
import com.humara.nagar.shared_pref.AppPreference
import com.humara.nagar.shared_pref.UserPreference
import com.humara.nagar.ui.ForceLogoutActivity

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
    }

    private fun initializeAppPreference(): AppPreference {
        return AppPreference(this)
    }

    private fun initializeUserPreference(): UserPreference {
        return UserPreference(this)
    }

    fun logout() {
        Logger.debugLog("Saurabh", "Logout")
        userSharedPreference.clearAll()
        appSharedPreference.logOut(false)
        SplashActivity.start(this)
    }

    fun showUnAuthorizedAPICallForceLogoutScreen(context: Context, source: String) {
        ForceLogoutActivity.startForceLogoutActivity(context, source)
    }
}