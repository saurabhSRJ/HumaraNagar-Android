package com.humara.nagar.fcm

import android.os.Bundle
import com.humara.nagar.R
import com.humara.nagar.constants.IntentKeyConstants
import com.humara.nagar.constants.NotificationConstants

object NotificationDeeplinkUtils {
    fun getDeeplinkArgument(extras: Bundle, target: String?): Bundle {
        return when (target) {
            NotificationDestinations.COMPLAINT_DETAILS.name -> {
                extras.apply {
                    putString(IntentKeyConstants.COMPLAINT_ID, extras.getString(NotificationConstants.ID))
                }
            }
            NotificationDestinations.POST_DETAILS.name -> {
                extras.apply {
                    putLong(IntentKeyConstants.POST_ID, extras.getString(NotificationConstants.POST_ID)?.toLong() ?: 0L)
                    putLong(IntentKeyConstants.AUTHOR_ID, extras.getString(NotificationConstants.AUTHOR_ID)?.toLong() ?: 0L)
                }
            }
            else -> extras
        }
    }

    fun getDeeplinkDestination(target: String?): Int {
        return when (target) {
            NotificationDestinations.COMPLAINT_DETAILS.name -> NotificationDestinations.COMPLAINT_DETAILS.destinationId
            NotificationDestinations.POST_DETAILS.name -> NotificationDestinations.POST_DETAILS.destinationId
            else -> NotificationDestinations.HOME.destinationId
        }
    }
}

enum class NotificationDestinations(val destinationId: Int) {
    HOME(R.id.homeFragment),
    COMPLAINT_DETAILS(R.id.complaintStatusFragment),
    POST_DETAILS(R.id.postDetailsFragment)
}