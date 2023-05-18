package com.humara.nagar.utils

import android.content.Context
import com.humara.nagar.R

object ComplaintsUtils {
    enum class ComplaintState(val currentState: String) {
        SENT("sent"),
        IN_PROGRESS("inprogress"),
        RESOLVED("resolved"),
        WITHDRAWN("withdrawn");

        companion object {
            fun getName(state: String, context: Context): String {
                return when (state) {
                    SENT.currentState -> context.getString(R.string.sent)
                    IN_PROGRESS.currentState -> context.getString(R.string.inprogress)
                    RESOLVED.currentState -> context.getString(R.string.resolved)
                    WITHDRAWN.currentState -> context.getString(R.string.withdrawn)
                    else -> ""
                }
            }

            fun getStateColor(state: String): Int {
                return when (state) {
                    SENT.currentState -> R.color.stroke_red
                    IN_PROGRESS.currentState -> R.color.stroke_yellow
                    RESOLVED.currentState -> R.color.stroke_green
                    WITHDRAWN.currentState -> R.color.blue_4285F4
                    else -> R.color.stroke_red
                }
            }

            fun getCtaText(state: String, context: Context, isUserAdmin: Boolean, ratingPresent: Boolean): String {
                return when (state) {
                    SENT.currentState, IN_PROGRESS.currentState -> if (isUserAdmin) context.getString(R.string.update) else context.getString(R.string.track)
                    RESOLVED.currentState, WITHDRAWN.currentState -> {
                        if (isUserAdmin) context.getString(R.string.details)
                        else if (ratingPresent) context.getString(R.string.details)
                        else context.getString(R.string.review)
                    }
                    else -> ""
                }
            }
        }
    }
}