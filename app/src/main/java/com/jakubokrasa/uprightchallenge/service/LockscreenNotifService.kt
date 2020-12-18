package com.jakubokrasa.uprightchallenge.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.jakubokrasa.uprightchallenge.BuildConfig
import com.jakubokrasa.uprightchallenge.R
import com.jakubokrasa.uprightchallenge.ui.MainActivity
import com.jakubokrasa.uprightchallenge.ui.SettingsFragment.Companion.NOTIFICATION_ID


class LockscreenNotifService : Service() {
    private lateinit var mNotificationManager: NotificationManager

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handleNotifIntent(intent)
        return START_STICKY
    }

    private fun handleNotifIntent(intent: Intent?) {
        mNotificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        deliverNotification(applicationContext)
        Log.d(LOG_TAG, "notification fired")
    }

    private fun deliverNotification(context: Context?) {
        //this intent causes that when the notification is clicked, the MainActivity is launched
        val notifClickIntent = Intent(context, MainActivity::class.java)
        val notifClickPendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                notifClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        // after you click action button on notification, the intent will be sent
        val postureYesIntent = Intent(context, RepeatingNotifService::class.java)
        postureYesIntent.action = RepeatingNotifService.GOOD_POSTURE_ACTION
        val yesPendingIntent = PendingIntent.getService(context, NOTIFICATION_ID, postureYesIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val postureNoIntent = Intent(context, RepeatingNotifService::class.java)
        postureNoIntent.action = RepeatingNotifService.BAD_POSTURE_ACTION
        val noPendingIntent = PendingIntent.getService(context, NOTIFICATION_ID, postureNoIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val lockscreenNotif = RemoteViews(packageName, R.layout.lockscreen_notification)
        lockscreenNotif.setOnClickPendingIntent(R.id.notif_positive_btn, yesPendingIntent)
        lockscreenNotif.setOnClickPendingIntent(R.id.notif_negative_btn, noPendingIntent)

        val builder = NotificationCompat.Builder(context!!, PRIMARY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notify)
                .setAutoCancel(true) // closes the notification when user taps on it
                .setContentIntent(notifClickPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContent(lockscreenNotif)

        mNotificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    companion object {
        private const val PRIMARY_CHANNEL_ID = "primary_notification_channel"
        private val LOG_TAG = LockscreenNotifService::class.java.simpleName
    }
}