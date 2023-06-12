package com.humara.nagar.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.edit
import androidx.core.view.children
import com.humara.nagar.NagarApp
import com.humara.nagar.R
import com.humara.nagar.constants.Constants
import com.humara.nagar.shared_pref.AppPreference
import com.humara.nagar.shared_pref.UserPreference
import java.util.*

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
    edit {
        when (value) {
            is Boolean -> putBoolean(key, value)
            is Float -> putFloat(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is String -> putString(key, value)
            else -> {
                throw UnsupportedOperationException("SharedPreferences put() not support ${T::class.qualifiedName.toString()} type of data.")
            }
        }
    }
}

/**
 * For SDK >= 31 we should put immutability flag when using PendingIntent.
 *
 *  It is strongly recommended to use `FLAG_IMMUTABLE` when creating a `PendingIntent`.
 *  `FLAG_MUTABLE` should only be used when some functionality relies on modifying
 *  the underlying intent, e.g. any `PendingIntent` that needs to be used with inline reply or bubbles.
 */
fun Context.getBroadcastPendingIntent(
    notificationId: Int,
    intent: Intent,
    isMutable: Boolean = false
): PendingIntent {
    return PendingIntent.getBroadcast(applicationContext, notificationId, intent, getMutabilityFlags(isMutable))
}

/**
 * For SDK >= 31 we should put immutability flag when using PendingIntent.
 *
 *  It is strongly recommended to use `FLAG_IMMUTABLE` when creating a `PendingIntent`.
 *  `FLAG_MUTABLE` should only be used when some functionality relies on modifying
 *  the underlying intent, e.g. any `PendingIntent` that needs to be used with inline reply or bubbles.
 */
fun Context.getActivityPendingIntent(
    notificationId: Int,
    intent: Intent,
    isMutable: Boolean = false
): PendingIntent {
    return PendingIntent.getActivity(applicationContext, notificationId, intent, getMutabilityFlags(isMutable))
}

private fun getMutabilityFlags(isMutable: Boolean = false) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isMutable) {
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
} else {
    PendingIntent.FLAG_UPDATE_CURRENT
}

fun Context.getUserSharedPreferences(): UserPreference = (this.applicationContext as NagarApp).userSharedPreference
fun Context.getAppSharedPreferences(): AppPreference = (this.applicationContext as NagarApp).appSharedPreference

fun ViewGroup.saveChildViewStates(): SparseArray<Parcelable> {
    val childViewStates = SparseArray<Parcelable>()
    children.forEach { child -> child.saveHierarchyState(childViewStates) }
    return childViewStates
}

fun ViewGroup.restoreChildViewStates(childViewStates: SparseArray<Parcelable>) {
    children.forEach { child -> child.restoreHierarchyState(childViewStates) }
}

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}

fun TextView.setVisibilityAndText(text: String?) {
    if (text.isNullOrEmpty()) {
        visibility = View.GONE
    } else {
        visibility = View.VISIBLE
        this.text = text
    }
}

fun Context.getStringByLocale(@StringRes stringRes: Int, locale: Locale = Locale(Constants.HINDI)): String {
    val configuration = Configuration(resources.configuration)
    configuration.setLocale(locale)
    return createConfigurationContext(configuration).resources.getString(stringRes)
}