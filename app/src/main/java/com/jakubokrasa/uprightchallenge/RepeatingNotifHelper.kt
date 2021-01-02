package com.jakubokrasa.uprightchallenge

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import com.jakubokrasa.uprightchallenge.receiver.SaveStatsAlarmReceiver
import com.jakubokrasa.uprightchallenge.service.RepeatingNotifService
import com.jakubokrasa.uprightchallenge.service.RepeatingNotifService.Companion.DELIVER_REPEATING_NOTIF_ACTION
import com.jakubokrasa.uprightchallenge.service.RepeatingNotifService.Companion.SCHEDULED_NOTIF_OFF_ACTION
import com.jakubokrasa.uprightchallenge.service.RepeatingNotifService.Companion.SCHEDULED_NOTIF_ON_ACTION
import com.jakubokrasa.uprightchallenge.ui.SettingsFragment
import java.util.*

class RepeatingNotifHelper(private val context: Context) {
    private val preferences: SharedPreferences
    private val mAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    init {
        preferences = context.getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE)
    }

    fun setNotifAlarm() {
        val alarmIntent = Intent(context, RepeatingNotifService::class.java)
        alarmIntent.action = DELIVER_REPEATING_NOTIF_ACTION
        val alarmPendingIntent = PendingIntent.getService(context, SettingsFragment.NOTIFICATION_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT) //todo rename to NotifAlarmPendingIntent, similarly alarmIntent
        val repeatInterval = preferences.getLong("pref_key_interval", AlarmManager.INTERVAL_HALF_HOUR)
        val triggerTime = SystemClock.elapsedRealtime() + repeatInterval
        Log.d(LOG_TAG, "repeat interval: $repeatInterval")
        mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, repeatInterval, alarmPendingIntent)
    }

    fun cancelNotifAlarm() {
        val alarmIntent = Intent(context, RepeatingNotifService::class.java)
        alarmIntent.action = DELIVER_REPEATING_NOTIF_ACTION
        val alarmPendingIntent = PendingIntent.getService(context, SettingsFragment.NOTIFICATION_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        mAlarmManager.cancel(alarmPendingIntent)
    }

    fun turnOffNotifications() {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()
        cancelNotifAlarm() // cancel repeating intent messages for AlarmReceiver
    }

    // Set the alarm to start approximately at midnight. The alarm will be used to reset counters every night and save results in database
    fun setSaveStatsAlarm() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar[Calendar.HOUR_OF_DAY] = 0 // set calendar hour to 0 a.m.
        calendar[Calendar.MINUTE] = 0
        calendar.add(Calendar.DATE, 1)
        val saveAlarmIntent = Intent(context, SaveStatsAlarmReceiver::class.java)
        val saveAlarmPendingIntent = PendingIntent.getBroadcast(context, SettingsFragment.RESET_ALARM_ID, saveAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, saveAlarmPendingIntent)
    }

    fun setNotifOnTimeRange() {
        val notifOnTimeIntent = Intent(context, RepeatingNotifService::class.java)
        val notifOffTimeIntent = Intent(context, RepeatingNotifService::class.java)
        notifOnTimeIntent.action = SCHEDULED_NOTIF_ON_ACTION
        notifOffTimeIntent.action = SCHEDULED_NOTIF_OFF_ACTION
        val notifOnTimePendingIntent = PendingIntent.getService(context, SettingsFragment.NOTIF_ON_TIME_ALARM, notifOnTimeIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notifOffTimePendingIntent = PendingIntent.getService(context, SettingsFragment.NOTIF_OFF_TIME_ALARM, notifOffTimeIntent, PendingIntent.FLAG_UPDATE_CURRENT)
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