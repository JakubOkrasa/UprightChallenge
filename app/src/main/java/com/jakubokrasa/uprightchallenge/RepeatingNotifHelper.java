package com.jakubokrasa.uprightchallenge;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import com.jakubokrasa.uprightchallenge.receiver.NotifAlarmReceiver;
import com.jakubokrasa.uprightchallenge.receiver.ResetAlarmReceiver;
import com.jakubokrasa.uprightchallenge.service.RepeatingNotifService;

import java.util.Calendar;

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

    public void turnOnNotifications() { //todo consider name change
        setAlarmPendingIntent();
        setResetPendingIntent();
        setNightHoursAlarm(true); // set start night hours alarm (turn off notifications)
        setNightHoursAlarm(false); // set finish night hours alarm (turn on notifications)
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

    private void cancelAlarmPendingIntent() {
        Intent alarmIntent = new Intent(context, NotifAlarmReceiver.class);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (mAlarmManager!=null) {
            mAlarmManager.cancel(alarmPendingIntent);
        }
    }

    // Set the alarm to start approximately at midnight. The alarm will be used to reset counters every night and save results in database
    private void setResetPendingIntent() { // TODO: rename/extract this and similar methods in this class. There is not only pending intent set.
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

    private void setNightHoursAlarm(boolean nightHoursTurnedON) {
        String action = nightHoursTurnedON ? NIGHT_HOURS_ON_ACTION : NIGHT_HOURS_OFF_ACTION;
        Intent nightHoursIntent = new Intent(action);
        PendingIntent nightHoursPendingIntent = PendingIntent.getBroadcast(context, NIGHT_HOURS_ALARM_ID, nightHoursIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if(nightHoursTurnedON) {
            calendar.set(Calendar.HOUR, 21);
            calendar.set(Calendar.MINUTE, 0);
        } else {
            calendar.set(Calendar.HOUR, 7);
            calendar.set(Calendar.MINUTE, 30);
//            calendar.add(Calendar.DATE, 1); //uncomment if not debug todo handle cases when DATE + 1 is unwanted
        }
        mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, nightHoursPendingIntent);
    }
}
