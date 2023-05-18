package com.humara.nagar

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

object Logger {

    fun debugLog(tag: String?, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg)
        }
    }

    fun debugLog(msg: String) {
        if (BuildConfig.DEBUG) {
            Log.d("NagarApp", msg)
        }
    }

    fun logException(tag: String, exception: Exception, logLevel: LogLevel, logToCrashlytics: Boolean = false) {
        when (logLevel) {
            LogLevel.DEBUG -> Log.d(tag, null, exception)
            LogLevel.ERROR -> Log.e(tag, null, exception)
            LogLevel.INFO -> Log.i(tag, null, exception)
            LogLevel.VERBOSE -> Log.v(tag, null, exception)
            LogLevel.WARN -> Log.w(tag, null, exception)
        }
        if (logToCrashlytics) {
            FirebaseCrashlytics.getInstance().recordException(exception)
        }
    }

    enum class LogLevel {
        DEBUG,
        ERROR,
        INFO,
        VERBOSE,
        WARN
    }
}