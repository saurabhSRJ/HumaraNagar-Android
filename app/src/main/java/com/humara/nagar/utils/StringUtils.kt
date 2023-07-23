package com.humara.nagar.utils

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import java.util.*

object StringUtils {
    /**
     * Function which returns a modified string with consecutive whitespace characters like space, tab and enter replaced by a single space for [input] string.
     */
    fun replaceWhitespaces(input: String): String {
        return input.trim().replace("\\s+".toRegex(), " ")
    }

    fun String.convertToLowerCase(): String {
        return replaceWhitespaces(this).lowercase()
    }

    fun toStringWithoutSpaces(inputString: String): String {
        return inputString.filter { it.isLetterOrDigit() }
    }

    fun showDotStringAfterLimitReached(maxLength: Int, input: String): String {
        return if (input.length > maxLength) {
            "${input.take(maxLength)}..."
        } else {
            input
        }
    }

    fun String.capitalizeEachWord(): String {
        return this.split(' ').joinToString(" ") { word -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() } }
    }

    /**
     * Converts a comma-separated string into a list of individual strings.
     */
    fun convertToList(input: String?): List<String> {
        if (input == null) return listOf()
        return input.split(",").map { it.trim() }
    }

    /**
     * Sets the text of the TextView with two portions of text, each with a different color.
     *
     * @param text1 The first portion of the text.
     * @param text2 The second portion of the text.
     * @param color1 The color for the first portion of the text.
     * @param color2 The color for the second portion of the text.
     */
    fun TextView.setStringWithColors(text1: String, text2: String, color1: Int, color2: Int) {
        val spannable = SpannableString(text1 + text2)
        spannable.setSpan(
            ForegroundColorSpan(color1),
            0,
            text1.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            ForegroundColorSpan(color2),
            text1.length,
            text2.length + text1.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        text = spannable
    }

    /**
     * Sets the text of the TextView with a specified color.
     *
     * @param text The text to be displayed.
     * @param color The color of the text.
     */
    fun TextView.setStringWithColor(text: String, color: Int) {
        val spannable = SpannableString(text)
        spannable.setSpan(
            ForegroundColorSpan(color),
            0,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        this.text = spannable
    }

    /**
     * Sets the typeface of a substring within the text of the TextView.
     *
     * @param startIndex The starting index of the substring.
     * @param endIndex The ending index of the substring.
     * @param input The input string on which the modification is applied.
     * @param typeface The desired typeface for the substring.
     */
    fun TextView.setStringWithTypeface(startIndex: Int, endIndex: Int, input: String, typeface: Typeface?) {
        val spannable = SpannableString(input)
        // Check if the provided indices are valid
        if (startIndex in 0 until endIndex && endIndex <= input.length) {
            spannable.setSpan(
                StyleSpan(typeface?.style ?: 0),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        this.text = spannable
    }

    fun TextView.setEndDrawable(string: String?, @DrawableRes drawableRes: Int) {
        val lineHeight = lineHeight
        val drawable = ContextCompat.getDrawable(context, drawableRes)
        drawable?.setBounds(0, 0, lineHeight, lineHeight)
        val builder = SpannableStringBuilder()
        builder.append(string ?: text).append("  ")
        builder.append(" ", ImageSpan(drawable!!), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        text = builder
    }

    /**
     * Method to set a background span on textView from start to end index
     * @param text
     * @param color
     * @param start
     * @param end
     * @return
     */
    fun getStringWithBackgroundColor(text: String?, color: Int, start: Int, end: Int): Spannable {
        val spannable = SpannableString(text)
        spannable.setSpan(BackgroundColorSpan(color), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        return spannable
    }
}