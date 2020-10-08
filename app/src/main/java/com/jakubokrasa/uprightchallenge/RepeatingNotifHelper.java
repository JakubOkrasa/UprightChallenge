package com.jakubokrasa.uprightchallenge;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.jakubokrasa.uprightchallenge.receiver.NotifAlarmReceiver;
import com.jakubokrasa.uprightchallenge.receiver.ResetAlarmReceiver;
import com.jakubokrasa.uprightchallenge.service.RepeatingNotifService;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static com.jakubokrasa.uprightchallenge.ui.SettingsFragment.NIGHT_HOURS_ALARM_ID;
import static com.jakubokrasa.uprightchallenge.ui.SettingsFragment.NIGHT_HOURS_OFF_ACTION;
import static com.jakubokrasa.uprightchallenge.ui.SettingsFragment.NIGHT_HOURS_ON_ACTION;
import static com.jakubokrasa.uprightchallenge.ui.SettingsFragment.NOTIFICATION_ID;
import static com.jakubokrasa.uprightchallenge.ui.SettingsFragment.RESET_ALARM_ID;

public class RepeatingNotifHelper {
    private SharedPreferences preferences;
    public static final String sharedPrefsFile = BuildConfig.APPLICATION_ID;
    private Context context;
    private final String LOG_TAG = RepeatingNotifHelper.class.getSimpleName();

    public RepeatingNotifHelper(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(sharedPrefsFile, MODE_PRIVATE);
    }

    public void turnOffNotifications() { //todo consider name change
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
        cancelAlarmPendingIntent(); // cancel repeating intent messages for AlarmReceiver
    }

    public void setAlarmPendingIntent() { //todo consider name change
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, NotifAlarmReceiver.class);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT); //todo rename to NotifAlarmPendingIntent, similarly alarmIntent
        long repeatInterval = preferences.getLong("pref_key_interval", AlarmManager.INTERVAL_HALF_HOUR);
        long triggerTime = SystemClock.elapsedRealtime() + repeatInterval;
        Log.d(LOG_TAG, "repeat interval: " + repeatInterval);
        if (mAlarmManager != null) {
            mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, repeatInterval, alarmPendingIntent);
        }
    }

    public void cancelAlarmPendingIntent() {
        Intent alarmIntent = new Intent(context, NotifAlarmReceiver.class);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (mAlarmManager!=null) {
            mAlarmManager.cancel(alarmPendingIntent);
        }
    }

    public void setResetPendingIntent() { // TODO: rename/extract this and similar methods in this class. There is not only pending intent set.
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0); // set calendar hour to 0 a.m.
        calendar.set(Calendar.MINUTE, 0);
        calendar.add(Calendar.DATE, 1);
//        calendar.set(Calendar.MINUTE, 29); //for debug only
        Intent resetAlarmIntent = new Intent(context, ResetAlarmReceiver.class);
        PendingIntent resetAlarmPendingIntent = PendingIntent.getBroadcast(context, RESET_ALARM_ID, resetAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, resetAlarmPendingIntent);
    }

    // Set the alarm to start approximately at midnight. The alarm will be used to reset counters every night and save results in database
    // TODO: 10/2/2020 not always works - fix that
    public void setNightHoursAlarm() {
        Intent nightHoursOnIntent = new Intent(NIGHT_HOURS_ON_ACTION);
        Intent nightHoursOffIntent = new Intent(NIGHT_HOURS_OFF_ACTION);
        PendingIntent nightHoursOnPendingIntent = PendingIntent.getBroadcast(context, NIGHT_HOURS_ALARM_ID, nightHoursOnIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent nightHoursOffPendingIntent = PendingIntent.getBroadcast(context, NIGHT_HOURS_ALARM_ID, nightHoursOffIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();

        //set turn off notifications time
        calendar.setTimeInMillis(System.currentTimeMillis());
        String nightEnd = preferences.getString("pref_key_night_hours_end", "7:30");
        Log.d(LOG_TAG, "notif begin time: " + nightEnd);
        String[] time = nightEnd.split(":");
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(time[1]);
        mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, nightHoursOnPendingIntent);

        //set turn on notifications time
        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 30);

//            calendar.add(Calendar.DATE, 1); //uncomment if not debug todo handle cases when DATE + 1 is unwanted
        mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, nightHoursOffPendingIntent);

    }
}
