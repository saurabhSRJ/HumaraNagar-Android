package com.example.humaranagar.utils

import java.util.regex.Pattern

class UserDataValidator {
    companion object {
        fun isValidMobileNumber(phone: String): Boolean {
            val phone1 = phone.trim { it <= ' ' }
            val pattern = Pattern.compile("^[6789][0-9]{9}$")
            val matcher = pattern.matcher(phone1)
            return matcher.matches()
        }
    }
}