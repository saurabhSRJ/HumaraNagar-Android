package com.example.humaranagar.shared_pref

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

open class SharedPreference(context: Context, preferenceName: String) : IPreferences {
    private val gson = Gson()

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        preferenceName, Context.MODE_PRIVATE
    )

    override fun getString(key: String, defaultValue: String?): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    override fun putString(key: String, value: String?) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    override fun putInt(key: String, value: Int?) {
        if (value == null)
            remove(key)
        else
            sharedPreferences.edit().putInt(key, value).apply()
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }

    override fun putLong(key: String, value: Long?) {
        if (value == null)
            remove(key)
        else
            sharedPreferences.edit().putLong(key, value).apply()
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return sharedPreferences.getFloat(key, defaultValue)
    }

    override fun putFloat(key: String, value: Float?) {
        if (value == null)
            remove(key)
        else
            sharedPreferences.edit().putFloat(key, value).apply()
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    override fun putBoolean(key: String, value: Boolean?) {
        if (value == null)
            remove(key)
        else
            sharedPreferences.edit().putBoolean(key, value).apply()
    }

    override fun getPreference() = sharedPreferences

    override fun getGsonParser() = gson

    override fun clearLocalCacheData(key: String?) {
        //NA
    }
}