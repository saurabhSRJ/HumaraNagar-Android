package com.humara.nagar.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import com.humara.nagar.R
import com.humara.nagar.constants.IntentKeyConstants
import com.humara.nagar.constants.NotificationConstants
import com.humara.nagar.fcm.NotificationDismissReceiver
import com.humara.nagar.ui.MainActivity
import java.util.*

object NotificationUtils {
    private const val DEFAULT_NOTIFICATION_CHANNEL_ID = "NAGAR_DEFAULT"

    fun displayNotification(context: Context, pushPayload: Map<String, String>) {
        // notificationId is a unique int for each notification that you must define
        val notificationId = createNotificationId()
        val pushPayloadBundle = Utils.convertMapToBundle(pushPayload)
        // empty notification
        if (pushPayload.containsKey(NotificationConstants.TITLE).not()) {
            return
        }
        val notificationBuilder = getNotificationBuilder(
            pushPayload[NotificationConstants.TITLE] ?: "",
            pushPayload[NotificationConstants.MESSAGE] ?: "",
            getNotificationChanelId(pushPayload[NotificationConstants.CHANNEL_ID] ?: DEFAULT_NOTIFICATION_CHANNEL_ID, context),
            pushPayloadBundle,
            notificationId,
            context
        )
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, notificationBuilder.build())
        }
    }

    private fun getNotificationBuilder(title: String, message: String, channelId: String, extras: Bundle, notificationId: Int, context: Context): NotificationCompat.Builder {
        val notificationCompatBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_add_button)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET) // content visibility on lock screen
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDeleteIntent(createOnDismissedIntent(notificationId, extras, context))
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(getNotificationPendingIntent(context, notificationId, extras))
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationCompatBuilder.setSound(getNotificationSoundUri(context))
        }
        return notificationCompatBuilder
    }

    /**
     * This method will extract NOTIFICATION_CHANNEL_ID, then validate it, if extracted id is valid one than
     * it will be returned as it is.
     */
    private fun getNotificationChanelId(extractedId: String, context: Context): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return ""
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.getNotificationChannel(extractedId) == null) {
            return DEFAULT_NOTIFICATION_CHANNEL_ID
        }
        return extractedId
    }

    private fun getNotificationPendingIntent(context: Context, notificationId: Int, extras: Bundle): PendingIntent {
        extras.apply {
            putString(IntentKeyConstants.SOURCE, "Notification-$notificationId")
        }
        return NavDeepLinkBuilder(context)
            .setGraph(R.navigation.mobile_navigation)
            .setDestination(R.id.navigation_home)
            .setArguments(extras)
            .setComponentName(MainActivity::class.java)
            .createPendingIntent()
    }

    private fun createOnDismissedIntent(notificationId: Int, extra: Bundle, context: Context): PendingIntent {
        val intent = Intent(context, NotificationDismissReceiver::class.java).apply {
            putExtras(extra)
            action = NotificationConstants.BROADCAST_NOTIFICATION_DISMISS_ACTION
        }
        return context.getBroadcastPendingIntent(notificationId, intent, isMutable = false)
    }

    private fun createNotificationId(): Int {
        return Random().nextInt(Int.MAX_VALUE - 1) + 1
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setupNotificationChannels(context: Context, notificationChannels: List<NotificationChannel>) {
        val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(context)
        notificationChannels.forEach { channel ->
            if (notificationManager.getNotificationChannel(channel.id) == null) {
                val attributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                channel.apply {
                    setSound(getNotificationSoundUri(context), attributes)
                    setShowBadge(true)
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }
            /**
             * Whenever Deleting/Updating an existing NotificationChannel is required then,
             * Write here the else part of above by using NotificationManager
             */
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setupDefaultNotificationChannel(context: Context) {
        val channel = NotificationChannel(DEFAULT_NOTIFICATION_CHANNEL_ID, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH)
        setupNotificationChannels(context, listOf(channel))
    }

    fun clearAllNotification(context: Context?) {
        NotificationManagerCompat.from(context!!).cancelAll()
    }

    private fun getNotificationSoundUri(context: Context): Uri {
        val appAndroidResourcePath = ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName + "/"
        return try {
            Uri.parse(appAndroidResourcePath + "raw/default_notif")
        } catch (ex: Exception) {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
    }
}