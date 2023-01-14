package com.example.humaranagar.utils

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan

/**
 * Utils class
 */
class Utils {
    companion object {
        fun getStringWithColors(
            text1: String,
            text2: String,
            color1: Int,
            color2: Int
        ): SpannableString {
            val spannable = SpannableString(text1 + text2)
            spannable.setSpan(ForegroundColorSpan(color1), 0, text1.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(ForegroundColorSpan(color2), text1.length, text2.length + text1.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannable
        }

        fun getStringWithColor(text: String, color: Int): SpannableString {
            val spannable = SpannableString(text)
            spannable.setSpan(
                ForegroundColorSpan(color),
                0,
                text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return spannable
        }
    }
}