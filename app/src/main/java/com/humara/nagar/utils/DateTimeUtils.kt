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
            // Parse the original date string
            val date = LocalDate.parse(dateString, originalFormatter)
            // Format the parsed date to the target format
            date.format(targetFormatter)
        } catch (e: DateTimeParseException) {
            e.printStackTrace()
            "" // Handle parsing exception
        }
    }

}