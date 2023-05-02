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
         * Function which returns a modified string with consecutive whitespace characters replaced by a single space for [input] string.
         */
        fun replaceWhitespace(input: String): String {
            return input.replace("\\s+".toRegex(), " ")
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

        fun makeCallViaIntent(context: Context, phoneNumber: String) {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$phoneNumber")
            context.startActivity(intent)
        }
        fun saveMediaToStorage(bitmap: Bitmap, context: Context): Boolean {
            var success = false
            val filename = "${System.currentTimeMillis()}.jpg"
            var fos: OutputStream? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver?.also { resolver ->
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }
                    val imageUri: Uri? =
                        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    fos = imageUri?.let { resolver.openOutputStream(it) }
                }
            } else {
                val imagesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val image = File(imagesDir, filename)
                fos = FileOutputStream(image)
            }
            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                Logger.debugLog("Saved to Photos")
                success = true
            }
            return success
        }

        fun toStringWithoutSpaces(inputString: String) : String {
            val stringBuilder = StringBuilder()
            for (char in inputString.toCharArray())
                if (char.isDigit() or char.isLetter())
                    stringBuilder.append(char)
            return stringBuilder.toString()
        }

        fun showDotStringAfterLimitReached(length: Int, string: String) : String {
            val stringBuilder = StringBuilder()
            Logger.debugLog("string: $string")
           for (i in 0 until length) {
                stringBuilder.append(string[i])
           }

            return stringBuilder.append("...").toString()
        }
    }
}