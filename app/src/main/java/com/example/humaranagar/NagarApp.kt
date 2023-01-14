package com.example.humaranagar

import android.app.Application
import com.example.humaranagar.shared_pref.AppPreference
import com.example.humaranagar.shared_pref.UserPreference

class NagarApp: Application() {
    lateinit var appSharedPreference: AppPreference
    lateinit var userSharedPreference: UserPreference

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
}