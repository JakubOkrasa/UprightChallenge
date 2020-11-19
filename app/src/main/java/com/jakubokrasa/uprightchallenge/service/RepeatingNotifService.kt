package com.jakubokrasa.uprightchallenge.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.jakubokrasa.uprightchallenge.BuildConfig
import com.jakubokrasa.uprightchallenge.RepeatingNotifHelper
import com.jakubokrasa.uprightchallenge.ui.SettingsFragment
import com.jakubokrasa.uprightchallenge.getTime

class RepeatingNotifService : Service() {
    private lateinit var mNotifyManager: NotificationManager
    private var notifHelper: RepeatingNotifHelper? = null
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val filter = IntentFilter()
        filter.addAction(SettingsFragment.SCHEDULED_NOTIF_OFF_ACTION)
        filter.addAction(SettingsFragment.SCHEDULED_NOTIF_ON_ACTION)
        registerReceiver(notifOnTimeReceiver, filter)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        handlePostureIntent(intent)
        return START_STICKY
    }

    override fun onDestroy() {
        unregisterReceiver(notifOnTimeReceiver)
        super.onDestroy()
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
            mNotifyManager!!.createNotificationChannel(notificationChannel)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun handlePostureIntent(intent: Intent) {
        if (intent.action == null) {
            return
        }
        val sharedPrefFile = BuildConfig.APPLICATION_ID
        val preferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE)
        val prefEditor = preferences.edit()
        mNotifyManager!!.cancel(SettingsFragment.NOTIFICATION_ID)
        if (intent.action == GOOD_POSTURE_ACTION) {
            Log.d(LOG_TAG, "yes option clicked")
            mNotifyManager!!.cancel(SettingsFragment.NOTIFICATION_ID)
            var count = preferences.getInt(PREF_KEY_GOOD_POSTURE_COUNT, 0)
            prefEditor.putInt(PREF_KEY_GOOD_POSTURE_COUNT, ++count).apply()
        } else if (intent.action == BAD_POSTURE_ACTION) {
            Log.d(LOG_TAG, "no option clicked")
            mNotifyManager!!.cancel(SettingsFragment.NOTIFICATION_ID)
            var count = preferences.getInt(PREF_KEY_BAD_POSTURE_COUNT, 0)
            prefEditor.putInt(PREF_KEY_BAD_POSTURE_COUNT, ++count).apply()
        }
    }

    // receives when is time to switch on/off notifications (user chooses time when he will be given notifications, usually notifications at night are unwanted)
    var notifOnTimeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (notifHelper == null) {
                notifHelper = RepeatingNotifHelper(context)
            }
            val preferences = context.getSharedPreferences(SettingsFragment.sharedPrefsFile, MODE_PRIVATE)
            val prefEditor = preferences.edit()
            if (intent.action == SettingsFragment.SCHEDULED_NOTIF_OFF_ACTION) {
                Log.d(LOG_TAG, getTime() + " action notif OFF received")
                prefEditor.putBoolean("pref_key_switch_notifications", false)
                notifHelper!!.cancelAlarmPendingIntent()
                mNotifyManager!!.cancelAll()
            } else if (intent.action == SettingsFragment.SCHEDULED_NOTIF_ON_ACTION) {
                Log.d(LOG_TAG, getTime() + " action notif ON received")
                prefEditor.putBoolean("pref_key_switch_notifications", true)
                notifHelper!!.setAlarmPendingIntent()
            }
            prefEditor.apply()
        }
    }

    companion object {
        // TODO: 10/1/2020 rename to PostureNotifService
        private const val PRIMARY_CHANNEL_ID = "primary_notification_channel"
        private const val PREF_KEY_GOOD_POSTURE_COUNT = "good_posture_count"
        private const val PREF_KEY_BAD_POSTURE_COUNT = "bad_posture_count"
        const val GOOD_POSTURE_ACTION = BuildConfig.APPLICATION_ID + ".GOOD_POSTURE_ACTION"
        const val BAD_POSTURE_ACTION = BuildConfig.APPLICATION_ID + ".BAD_POSTURE_ACTION"
        private val LOG_TAG = RepeatingNotifService::class.java.simpleName
    }
}