package com.humara.nagar.utils

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

        fun formatImageString(imageTitle: String): String {
            return imageTitle
                .substring(0, 2) + ".." + imageTitle
                .substring(imageTitle.length - 3)
        }

        fun convertMapToBundle(map: Map<String, String>): Bundle {
            val bundle = Bundle()
            for ((key, value) in map) {
                bundle.putString(key, value)
            }
            return bundle
        }
    }
}