package com.humara.nagar.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object DateTimeUtils {
    /**
     * Converts a date string from the original format to the target format.
     *
     * @param dateString The date string to convert.
     * @param originalFormat The original format of the date string.
     * @param targetFormat The target format to convert the date string to.
     * @return The converted date string in the target format, or an empty string if the conversion fails.
     */
    fun convertDateFormat(dateString: String, originalFormat: String, targetFormat: String): String {
        val originalFormatter = DateTimeFormatter.ofPattern(originalFormat)
        val targetFormatter = DateTimeFormatter.ofPattern(targetFormat)
        return try {
            val date = LocalDate.parse(dateString, originalFormatter)
            date.format(targetFormatter)
        } catch (e: DateTimeParseException) {
            e.printStackTrace()
            "" // Handle parsing exception
        }
    }

    /**
     * Retrieves a date that is earlier than the current date by the specified number of years.
     *
     * @param yearsToSubtract The number of years to subtract from the current date.
     * @return A Triple containing the year, month, and day of the earlier date.
     */
    fun getEarlierDate(yearsToSubtract: Long): Triple<Int, Int, Int> {
        val currentDate = LocalDate.now()
        val earlierDate = currentDate.minusYears(yearsToSubtract)
        val year = earlierDate.year
        val month = earlierDate.monthValue
        val day = earlierDate.dayOfMonth
        return Triple(year, month, day)
    }
}