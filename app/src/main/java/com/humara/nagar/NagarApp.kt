package com.humara.nagar

import android.app.Application
import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.analytics.AnalyticsTracker
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.shared_pref.AppPreference
import com.humara.nagar.shared_pref.UserPreference
import com.humara.nagar.ui.AppConfigViewModel
import com.humara.nagar.ui.ForceLogoutActivity
import org.json.JSONObject

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
        AnalyticsTracker.getInstance()
    }

    private fun initializeAppPreference(): AppPreference {
        return AppPreference(this)
    }

    private fun initializeUserPreference(): UserPreference {
        return UserPreference(this)
    }

    fun logout(activity: FragmentActivity, source: String) {
        ViewModelProvider(activity.viewModelStore, ViewModelFactory())[AppConfigViewModel::class.java].logout()
        AnalyticsTracker.sendEvent(AnalyticsData.EventName.LOGOUT, JSONObject().put(AnalyticsData.Parameters.SOURCE, source))
        userSharedPreference.clearAll()
        appSharedPreference.logOut(false)
        SplashActivity.start(this)
    }

    fun showUnAuthorizedAPICallForceLogoutScreen(context: Context, source: String) {
        ForceLogoutActivity.startForceLogoutActivity(context, source)
    }
}