package com.example.humaranagar

import android.util.Log

object Logger {

    @JvmStatic
    fun debugLog(tag: String?, msg: String?) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg!!)
        }
    }

    @JvmStatic
    fun debugLog(msg: String?) {
        if (BuildConfig.DEBUG) {
            Log.d("Log", msg!!)
        }
    }

    @JvmStatic
    fun logException(tag: String, exception: Exception, logLevel: LogLevel, logToCrashlytics : Boolean) {
        when (logLevel) {
            LogLevel.DEBUG -> Log.d(tag, null, exception)
            LogLevel.ERROR -> Log.e(tag, null, exception)
            LogLevel.INFO -> Log.i(tag, null, exception)
            LogLevel.VERBOSE -> Log.v(tag, null, exception)
            LogLevel.WARN -> Log.w(tag, null, exception)
        }
        if (logToCrashlytics) {
            //TODO: send log to crashlytics like Firebase
        }
    }

    @JvmStatic
    fun logException(tag: String, exception: Exception, logLevel: LogLevel) {
        logException(tag, exception, logLevel, false)
    }

    enum class LogLevel {
        DEBUG, ERROR, INFO, VERBOSE, WARN
    }
}