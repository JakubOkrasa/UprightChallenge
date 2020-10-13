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

import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;
import static com.jakubokrasa.uprightchallenge.ui.SettingsFragment.NIGHT_HOURS_ALARM_ID;
import static com.jakubokrasa.uprightchallenge.ui.SettingsFragment.NOTIF_ON_TIME_ACTION;
import static com.jakubokrasa.uprightchallenge.ui.SettingsFragment.NOTIF_OFF_TIME_ACTION;
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

    // Set the alarm to start approximately at midnight. The alarm will be used to reset counters every night and save results in database
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

    public void setNotifOnTimeRange() {
        Intent notifOnTimeIntent = new Intent(NOTIF_ON_TIME_ACTION);
        Intent notifOffTimeIntent = new Intent(NOTIF_OFF_TIME_ACTION);
        PendingIntent notifOnTimePendingIntent = PendingIntent.getBroadcast(context, NIGHT_HOURS_ALARM_ID, notifOnTimeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent notifOffTimePendingIntent = PendingIntent.getBroadcast(context, NIGHT_HOURS_ALARM_ID, notifOffTimeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();

        //set turn on notifications time
        calendar.setTimeInMillis(System.currentTimeMillis());
        String notifOnTime = preferences.getString(context.getResources().getString(R.string.pref_key_notif_on_time), "7:30");
        String notifOffTime = preferences.getString(context.getResources().getString(R.string.pref_key_notif_off_time), "21:00");
        // TODO: 10/12/2020 check if notifOnTime<notifOffTime, otherwise day+1
        // TODO: 10/12/2020 what if
        //  1. start nofif set at 10 
        //  2. end notif set at 7 (+1day)
        //  3. start notif set at 5 and now is 3.
        //  >> end notif will be on the next day
        Log.d(LOG_TAG, "notif begin time: " + notifOnTime);
        Log.d(LOG_TAG, "notif end time: " + notifOffTime);
        setCalendar(calendar, notifOnTime);
        mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, notifOnTimePendingIntent);

        //set turn off notifications time
        setCalendar(calendar, notifOffTime);

//            calendar.add(Calendar.DATE, 1); //uncomment if not debug todo handle cases when DATE + 1 is unwanted
        mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, notifOffTimePendingIntent);

    }

    private void setCalendar(Calendar calendar, String timePref) {
        String[] timeArr = timePref.split(":");
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArr[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeArr[1]));
    }

}
