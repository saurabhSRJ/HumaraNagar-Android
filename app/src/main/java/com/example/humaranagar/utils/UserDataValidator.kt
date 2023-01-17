package com.example.humaranagar.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class UserDataValidator {
    companion object {
        fun isValidMobileNumber(phone: String): Boolean {
            val phone1 = phone.trim { it <= ' ' }
            val pattern = Pattern.compile("^[56789][0-9]{9}$")
            val matcher = pattern.matcher(phone1)
            return matcher.matches()
        }

        fun isValidDateOfBirth(dob: String): Boolean {
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            val strDate = dateFormat.parse(dob)
            return strDate?.before(Date()) ?: false
        }
    }
}