package com.humara.nagar.utils

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import com.humara.nagar.R
import com.humara.nagar.constants.Constants

/**
 * Utils class
 */
class Utils {
    companion object {
        fun getMobileNumberWithCountryCode(mobileNumber: String): String {
            return Constants.COUNTRY_CODE.plus(" $mobileNumber")
        }

        fun convertMapToBundle(map: Map<String, String>): Bundle {
            val bundle = Bundle()
            for ((key, value) in map) {
                bundle.putString(key, value)
            }
            return bundle
        }

        /**
         * Returns Android screen width
         */
        fun getScreenWidth(): Int {
            return Resources.getSystem().displayMetrics.widthPixels
        }

        /**
         * Returns Android screen height excluding the navigation bar
         */
        fun getScreenHeight(): Int {
            return Resources.getSystem().displayMetrics.heightPixels
        }

        /**
         * @param input The input string to find the largest possible prefix substring from.
         * @param maxLength The maximum length that the substring should not exceed.
         * @param separator character to be used as separator
         * @return the largest substring that starts from the first character of the input string and has a maximum length of
         * [maxLength], or an empty string if the input string is empty or if there are no words that fit within the maximum length
         */
        fun findLargestPrefixSubstring(input: String, maxLength: Int, separator: String): String {
            val words = input.split(separator)
            val resultBuilder = StringBuilder()
            var currLength = 0
            // Concatenate the words into a separator-separated string until the maximum length is reached
            for (word in words) {
                if (currLength + word.length <= maxLength) {
                    resultBuilder.append(word).append(separator)
                    currLength += word.length + separator.length
                } else {
                    break
                }
            }
            return resultBuilder.toString().dropLast(separator.length)
        }

        fun shareViaIntent(activity: Activity, bitmap: Bitmap?, text: String?, imgTitle: String? = null) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                bitmap?.let {
                    val imgBitmapPath: String = MediaStore.Images.Media.insertImage(activity.contentResolver, bitmap,
                        imgTitle ?: "${activity.getString(R.string.app_name)}_${System.currentTimeMillis()}", "null")
                    val imgBitmapUri = Uri.parse(imgBitmapPath)
                    putExtra(Intent.EXTRA_STREAM, imgBitmapUri)
                    type = "image/jpeg"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                putExtra(Intent.EXTRA_TITLE, "Introducing content previews")
            }
            activity.startActivity(shareIntent)
        }
    }
}