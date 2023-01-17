package com.example.humaranagar.utils

import com.example.humaranagar.constants.Constants

/**
 * Utils class
 */
class Utils {
    companion object {
        fun getMobileNumberWithCountryCode(mobileNumber: String): String {
            return Constants.COUNTRY_CODE.plus(" $mobileNumber")
        }
    }
}