package com.humara.nagar.utils

import com.humara.nagar.constants.Constants
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
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
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val dateOfBirth = LocalDate.parse(dob, formatter)
            val currentDate = LocalDate.now()
            val age = Period.between(dateOfBirth, currentDate).years
            return age >= Constants.MIN_AGE_REQUIREMENT
        }
    }
}