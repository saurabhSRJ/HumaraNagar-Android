package com.humara.nagar.utils

import android.content.res.Resources
import android.os.Bundle
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
    }
}