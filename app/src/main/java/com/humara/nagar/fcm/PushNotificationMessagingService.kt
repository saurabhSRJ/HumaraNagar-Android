package com.humara.nagar.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.analytics.AnalyticsTracker
import com.humara.nagar.utils.NotificationUtils
import org.json.JSONObject

class PushNotificationMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val pushPayload = remoteMessage.data
        if (pushPayload.isEmpty()) return
        AnalyticsTracker.sendEvent(AnalyticsData.EventName.NOTIFICATION_RECEIVED, JSONObject().apply {
            put(AnalyticsData.Parameters.NOTIFICATION_DATA, pushPayload)
        })
        NotificationUtils.displayNotification(applicationContext, pushPayload)
    }

    override fun onNewToken(token: String) {
        FcmTokenUploadRepository(applicationContext).resetExistingTokenIfNotSameAsNew(token)
        // send the token to your server
    }

    companion object {
        private const val TAG = "PushNotificationMessagingService"
    }
}