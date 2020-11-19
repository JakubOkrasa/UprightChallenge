package com.jakubokrasa.uprightchallenge.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.jakubokrasa.uprightchallenge.R
import com.jakubokrasa.uprightchallenge.service.RepeatingNotifService
import com.jakubokrasa.uprightchallenge.ui.MainActivity

//receive periodically notifications pending intents
class NotifAlarmReceiver : BroadcastReceiver() {
    private lateinit var mNotificationManager: NotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        deliverNotification(context)
        Log.d(LOG_TAG, "notification fired")
    }

    private fun deliverNotification(context: Context) {
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
        val builder = NotificationCompat.Builder(context, PRIMARY_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.posture_notif_question))
                .setSmallIcon(R.drawable.ic_notify)
                .setAutoCancel(true) // closes the notification when user taps on it
                .setContentIntent(notifClickPendingIntent)
                .addAction(R.drawable.ic_posture_yes, context.getString(R.string.posture_notif_positive_label), yesPendingIntent)
                .addAction(R.drawable.ic_posture_no, context.getString(R.string.posture_notif_negative_label), noPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
        mNotificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    companion object {
        private const val NOTIFICATION_ID = 0
        private const val PRIMARY_CHANNEL_ID = "primary_notification_channel"
        private val LOG_TAG = NotifAlarmReceiver::class.java.simpleName
    }
}