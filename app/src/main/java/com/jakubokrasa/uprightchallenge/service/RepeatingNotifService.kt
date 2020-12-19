package com.jakubokrasa.uprightchallenge.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.jakubokrasa.uprightchallenge.BuildConfig
import com.jakubokrasa.uprightchallenge.R
import com.jakubokrasa.uprightchallenge.RepeatingNotifHelper
import com.jakubokrasa.uprightchallenge.getTime
import com.jakubokrasa.uprightchallenge.ui.MainActivity
import com.jakubokrasa.uprightchallenge.ui.SettingsFragment

class RepeatingNotifService : Service() {
    private lateinit var mNotifyManager: NotificationManager
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        handleNotifIntent(intent)
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun handleNotifIntent(intent: Intent) {
        if (intent.action == null) {
            return
        }
        val notifHelper = RepeatingNotifHelper(this)
        val preferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE)
        val prefEditor = preferences.edit()
        when (intent.action) {
            DELIVER_REPEATING_NOTIF_ACTION -> {
                deliverNotification(applicationContext)
                Log.d(LOG_TAG, "notification fired")
            }
            GOOD_POSTURE_ACTION -> {
                Log.d(LOG_TAG, "yes option clicked")
                mNotifyManager.cancel(SettingsFragment.NOTIFICATION_ID)
                var count = preferences.getInt(PREF_KEY_GOOD_POSTURE_COUNT, 0)
                prefEditor.putInt(PREF_KEY_GOOD_POSTURE_COUNT, ++count)
            }
            BAD_POSTURE_ACTION -> {
                Log.d(LOG_TAG, "no option clicked")
                mNotifyManager.cancel(SettingsFragment.NOTIFICATION_ID)
                var count = preferences.getInt(PREF_KEY_BAD_POSTURE_COUNT, 0)
                prefEditor.putInt(PREF_KEY_BAD_POSTURE_COUNT, ++count)
            }
            SCHEDULED_NOTIF_OFF_ACTION -> {
                Log.d(LOG_TAG, getTime() + " action notif OFF received")
                prefEditor.putBoolean("pref_key_switch_notifications", false)
                notifHelper.cancelAlarmPendingIntent()
                mNotifyManager.cancelAll()
            }
            SCHEDULED_NOTIF_ON_ACTION -> {
                Log.d(LOG_TAG, getTime() + " action notif ON received")
                prefEditor.putBoolean("pref_key_switch_notifications", true)
                notifHelper.setAlarmPendingIntent()
            }
        }
        prefEditor.apply()
    }

    private fun createNotificationChannel() {
        mNotifyManager = baseContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val vibPattern = longArrayOf(0, 200, 200, 200, 200, 200) //{delay1, vibDuration1, delay2, vibDuration2...}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                    PRIMARY_CHANNEL_ID,
                    "Check posture notification",
                    NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = "notification check posture"
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern = vibPattern
            mNotifyManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun deliverNotification(context: Context?) {
        //this intent causes that when the notification is clicked, the MainActivity is launched
        val notifClickIntent = Intent(context, MainActivity::class.java)
        val notifClickPendingIntent = PendingIntent.getActivity(
                context,
                SettingsFragment.NOTIFICATION_ID,
                notifClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        // after you click action button on notification, the intent will be sent
        val postureYesIntent = Intent(context, RepeatingNotifService::class.java)
        postureYesIntent.action = RepeatingNotifService.GOOD_POSTURE_ACTION
        val yesPendingIntent = PendingIntent.getService(context, SettingsFragment.NOTIFICATION_ID, postureYesIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val postureNoIntent = Intent(context, RepeatingNotifService::class.java)
        postureNoIntent.action = RepeatingNotifService.BAD_POSTURE_ACTION
        val noPendingIntent = PendingIntent.getService(context, SettingsFragment.NOTIFICATION_ID, postureNoIntent, PendingIntent.FLAG_UPDATE_CURRENT)

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

        mNotifyManager.notify(SettingsFragment.NOTIFICATION_ID, builder.build())
    }

    companion object {
        private const val PRIMARY_CHANNEL_ID = "primary_notification_channel"
        private const val PREF_KEY_GOOD_POSTURE_COUNT = "good_posture_count"
        private const val PREF_KEY_BAD_POSTURE_COUNT = "bad_posture_count"
        const val SCHEDULED_NOTIF_OFF_ACTION = BuildConfig.APPLICATION_ID + ".SCHEDULED_NOTIF_OFF_ACTION"
        const val SCHEDULED_NOTIF_ON_ACTION = BuildConfig.APPLICATION_ID + ".SCHEDULED_NOTIF_ON_ACTION"
        const val DELIVER_REPEATING_NOTIF_ACTION = BuildConfig.APPLICATION_ID + ".DELIVER_REPEATING_NOTIF_ACTION"
        const val GOOD_POSTURE_ACTION = BuildConfig.APPLICATION_ID + ".GOOD_POSTURE_ACTION"
        const val BAD_POSTURE_ACTION = BuildConfig.APPLICATION_ID + ".BAD_POSTURE_ACTION"
        private val LOG_TAG = RepeatingNotifService::class.java.simpleName
        const val sharedPrefFile = BuildConfig.APPLICATION_ID

    }
}