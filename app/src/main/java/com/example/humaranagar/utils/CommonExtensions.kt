package com.example.humaranagar.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.humaranagar.R


fun Context.showToast(message: String, shortToast: Boolean = false) {
    Toast.makeText(this, message, if (shortToast) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
}

fun View.setNonDuplicateClickListener(listener: View.OnClickListener?) {
    setOnClickListener {
        var lastClickTime: Long = 0
        if (getTag(R.id.TAG_CLICK_TIME) != null) {
            lastClickTime = getTag(R.id.TAG_CLICK_TIME) as Long
        }
        val curTime = System.currentTimeMillis()
        if (curTime - lastClickTime > context.resources.getInteger(R.integer.duplicate_click_delay)) {
            listener?.onClick(this)
            setTag(R.id.TAG_CLICK_TIME, curTime)
        }
    }
}

fun View.requestFocusAndShowKeyboard(inputMethodManager: InputMethodManager) {
    this.requestFocus()
    inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

/**
 * An inline function to put the value into the shared preferences with their respective key.
 */
inline fun <reified T> SharedPreferences.put(key: String, value: T) {
    val editor = this.edit()
    when (T::class) {
        Boolean::class -> editor.putBoolean(key, value as Boolean)
        Float::class -> editor.putFloat(key, value as Float)
        Int::class -> editor.putInt(key, value as Int)
        Long::class -> editor.putLong(key, value as Long)
        String::class -> editor.putString(key, value as String)
        else -> {
            if (value is Set<*>) {
                try {
                    editor.putStringSet(key, value as Set<String>)
                } catch (e: Exception) {
                    Log.e(
                        "CommonExtensions",
                        "Exception caught while putting Set<*> value as Set<String>.",
                        e
                    )
                }
            } else {
                throw UnsupportedOperationException(
                    "SharedPreferences put() not support ${T::class.qualifiedName.toString()} type of data."
                )
            }
        }
    }
    editor.apply()
}
