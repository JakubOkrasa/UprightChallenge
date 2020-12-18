package com.jakubokrasa.uprightchallenge

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.SystemClock
import android.util.Log
import com.jakubokrasa.uprightchallenge.receiver.ResetAlarmReceiver
import com.jakubokrasa.uprightchallenge.service.LockscreenNotifService
import com.jakubokrasa.uprightchallenge.service.RepeatingNotifService
import com.jakubokrasa.uprightchallenge.ui.SettingsFragment
import java.util.*

class RepeatingNotifHelper(private val context: Context) {
    private val preferences: SharedPreferences

    init {
        preferences = context.getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE)
    }

    fun turnOffNotifications() { //todo consider name change
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()
        cancelAlarmPendingIntent() // cancel repeating intent messages for AlarmReceiver
    }

    fun setAlarmPendingIntent() { //todo consider name change
        val mAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, LockscreenNotifService::class.java)
        val alarmPendingIntent = PendingIntent.getService(context, SettingsFragment.NOTIFICATION_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT) //todo rename to NotifAlarmPendingIntent, similarly alarmIntent
        val repeatInterval = preferences.getLong("pref_key_interval", AlarmManager.INTERVAL_HALF_HOUR)
        val triggerTime = SystemClock.elapsedRealtime() + repeatInterval
        Log.d(LOG_TAG, "repeat interval: $repeatInterval")
        mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, repeatInterval, alarmPendingIntent)
    }

    fun cancelAlarmPendingIntent() {
        val alarmIntent = Intent(context, LockscreenNotifService::class.java)
        val alarmPendingIntent = PendingIntent.getService(context, SettingsFragment.NOTIFICATION_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        val mAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mAlarmManager.cancel(alarmPendingIntent)
    }

    // Set the alarm to start approximately at midnight. The alarm will be used to reset counters every night and save results in database
    fun setResetPendingIntent() { // TODO: rename/extract this and similar methods in this class. There is not only pending intent set.
        val mAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar[Calendar.HOUR_OF_DAY] = 0 // set calendar hour to 0 a.m.
        calendar[Calendar.MINUTE] = 0
        calendar.add(Calendar.DATE, 1)
        val resetAlarmIntent = Intent(context, ResetAlarmReceiver::class.java)
        val resetAlarmPendingIntent = PendingIntent.getBroadcast(context, SettingsFragment.RESET_ALARM_ID, resetAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, resetAlarmPendingIntent)
    }

    fun setNotifOnTimeRange() {
        val notifOnTimeIntent = Intent(context, RepeatingNotifService::class.java)
        val notifOffTimeIntent = Intent(context, RepeatingNotifService::class.java)
        notifOnTimeIntent.action = SettingsFragment.SCHEDULED_NOTIF_ON_ACTION
        notifOffTimeIntent.action = SettingsFragment.SCHEDULED_NOTIF_OFF_ACTION
        val notifOnTimePendingIntent = PendingIntent.getService(context, SettingsFragment.NOTIF_ON_TIME_ALARM, notifOnTimeIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notifOffTimePendingIntent = PendingIntent.getService(context, SettingsFragment.NOTIF_OFF_TIME_ALARM, notifOffTimeIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val mAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val notifOnTime = preferences.getString(context.resources.getString(R.string.pref_key_notif_on_time), "7:30")
        val notifOffTime = preferences.getString(context.resources.getString(R.string.pref_key_notif_off_time), "21:00")
        val notifOnCal = getCalendar(notifOnTime)
        val notifOffCal = getCalendar(notifOffTime)
        if (notifOnCal.timeInMillis > notifOffCal.timeInMillis) {
            notifOffCal.add(Calendar.DATE, 1)
        }
        mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, notifOnCal.timeInMillis, AlarmManager.INTERVAL_DAY, notifOnTimePendingIntent)
        mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, notifOffCal.timeInMillis, AlarmManager.INTERVAL_DAY, notifOffTimePendingIntent)
        Log.d(LOG_TAG, "notif begin time: $notifOnTime")
        Log.d(LOG_TAG, "notif end time: $notifOffTime")
    }

    private fun getCalendar(timePref: String?): Calendar {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val timeArr = timePref!!.split(":").toTypedArray()
        calendar[Calendar.HOUR_OF_DAY] = timeArr[0].toInt()
        calendar[Calendar.MINUTE] = timeArr[1].toInt()
        return calendar
    }

    companion object {
        const val sharedPrefsFile = BuildConfig.APPLICATION_ID
        private val LOG_TAG = RepeatingNotifHelper::class.java.simpleName
    }

}