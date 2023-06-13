package com.humara.nagar.utils

import android.content.Context
import android.text.format.DateUtils
import com.humara.nagar.R
import java.time.*
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
     * Converts a date-time string in ISO 8601 format to the specified target format.
     *
     * @param dateTimeString The date-time string in ISO 8601 format.
     * @param targetFormat The target format pattern to convert the date-time string to.
     * @return The date-time string formatted in the target format, or an empty string if there was a parsing exception.
     */
    fun convertIsoDateTimeFormat(dateTimeString: String, targetFormat: String): String {
        val instant = Instant.parse(dateTimeString)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val targetFormatter = DateTimeFormatter.ofPattern(targetFormat)
        return try {
            dateTime.format(targetFormatter)
        } catch (e: DateTimeParseException) {
            // Handle parsing exception
            ""
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

    /**
     * Returns a relative duration string based on the current time compared to the given start time.
     *
     * @param context The context used for string resource retrieval.
     * @param startTime The start time in ISO 8601 format.
     * @return A string representing the relative duration from the start time to the current time.
     */
    fun getRelativeDurationFromCurrentTime(context: Context, startTime: String): String {
        val instant = Instant.parse(startTime)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val period = Period.between(dateTime.toLocalDate(), LocalDate.now())

        val days = period.days
        val weeks = days / 7
        val months = period.months
        val years = period.years

        return when {
            period.toTotalMonths() < 1 -> {
                // Within a month
                if (weeks < 1) {
                    // Within a week
                    when {
                        days < 1 -> {
                            // Within a day
                            val currentTimeMillis = System.currentTimeMillis()
                            val instantMillis = instant.toEpochMilli()
                            val diffInMs = System.currentTimeMillis() - instantMillis
                            if (diffInMs <= DateUtils.MINUTE_IN_MILLIS) {
                                context.getString(R.string.just_now)
                            } else {
                                DateUtils.getRelativeTimeSpanString(instantMillis, currentTimeMillis, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_TIME).toString()
                            }
                        }
                        else -> {
                            // Days ago
                            context.resources.getQuantityString(R.plurals.n_days_ago, days, days)
                        }
                    }
                } else {
                    // Weeks ago
                    context.resources.getQuantityString(R.plurals.n_weeks_ago, weeks, weeks)
                }
            }
            period.toTotalMonths() < 12 -> {
                // Months ago
                context.resources.getQuantityString(R.plurals.n_months_ago, months, months)
            }
            else -> {
                // Years ago
                context.resources.getQuantityString(R.plurals.n_years_ago, years, years)
            }
        }
    }

    fun isIsoTimeLessThanNow(isoTime: String): Boolean {
        val instant = Instant.parse(isoTime)
        val currentInstant = Instant.now()
        return instant.isBefore(currentInstant)
    }
}