package com.humara.nagar.fcm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.analytics.AnalyticsTracker
import org.json.JSONObject

class NotificationDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.extras?.let {
            AnalyticsTracker.sendEvent(AnalyticsData.EventName.NOTIFICATION_DISMISSED, JSONObject().apply {
                put(AnalyticsData.Parameters.NOTIFICATION_DATA, it.toString())
            })
        }
    }
}