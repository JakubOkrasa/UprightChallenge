package com.example.uprightchallenge;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import android.os.SystemClock;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

/**
 * A simple {@link Fragment} subclass.
 */

/*
//todo #note :
    After turning on notif, (default is 30 minutes interval) notifications appear very often, maybe even less than 30 seconds.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    private SharedPreferences preferences;
    public static final String sharedPrefsFile = BuildConfig.APPLICATION_ID;
    private SharedPreferences.Editor prefsEditor;
    private RepeatingNotifService repeatingNotifService;
    private final String LOG_TAG = SettingsFragment.class.getSimpleName();
    public static final int NOTIFICATION_ID = 0;


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        Intent startServiceIntent = new Intent(getContext(), RepeatingNotifService.class);
        requireContext().startService(startServiceIntent);

        setPreferencesFromResource(R.xml.preferences, rootKey);

        if(getActivity().getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE)!=null) {
            preferences = getActivity().getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE);
        }

        SwitchPreferenceCompat notificationSwitch = findPreference("pref_key_switch_notifications");
        ListPreference intervalListPref = findPreference("pref_key_interval"); //todo not sure if it can be final
//        ListPreference intervalListPref = findPreference(getResources().getString(R.string.interval_list_pref)); // this way (getResources()) doesn't work

        //set alarmPendingIntent to deliver repeating notifications
//        alarmPendingIntent = PendingIntent.getBroadcast(getContext(), NOTIFICATION_ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if(notificationSwitch!=null) {
            notificationSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object notificationsOnObject) {
                    boolean notificationsOn = (Boolean) notificationsOnObject;
                    ListPreference intervalListPref = findPreference("pref_key_interval");
                    if(notificationsOn) {
                        if(preferences!=null) {
                            setAlarmPendingIntent();
                        }
                        else {  //todo this should be avoided with setting defauld prefs ealier ( this 'if .. else' is only for debug)
                            Log.e(LOG_TAG, "AlarmPendingIntent not set (prefs were null)");
                        }
                    }
                    else {
                        repeatingNotifService.cancelNotifications();
                        cancelAlarmPendingIntent(); // cancel repeating intent messages for AlarmReceiver
                    }
                    // save changes to SharedPreferences
                    prefsEditor = preferences.edit();
                    prefsEditor.putBoolean(preference.getKey(), notificationsOn).apply();
                    intervalListPref.setEnabled(notificationsOn);
                    return true;
                }
            });
        }


        if(intervalListPref!=null) {
            Log.d(LOG_TAG,"intervalListPref: " + intervalListPref.toString());
            intervalListPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String interval =(String) newValue;
                    prefsEditor = preferences.edit();
                    prefsEditor.putLong(preference.getKey(), Long.parseLong(interval)).apply();
                    setAlarmPendingIntent();
                    return true;
                }
            });
        }
        else{
            Log.e(LOG_TAG, "preference interval is null");
        }

            //disable interval preference in UI if notifications are turned off
//            ListPreference notifIntervalListPref = findPreference("pref_key_interval");
//        if (intervalListPref != null)
          intervalListPref.setEnabled(preferences.getBoolean("pref_key_switch_notifications", false)); //todo fix crash
    }

    private void setAlarmPendingIntent() {
        Intent alarmIntent = new Intent(getContext(), AlarmReceiver.class);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(getContext(), NOTIFICATION_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT); //todo check if cancelling always onCreate doesn't cancel needed intents
        AlarmManager mAlarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        long repeatInterval = preferences.getLong("pref_key_interval", 1800000); //1800000ms = 30min
//        if (Build.FINGERPRINT.startsWith("google/sdk_gphone_x86/generic") || Build.FINGERPRINT.startsWith("samsung")) { repeatInterval = 30000; }//short interval only for debug
        long triggerTime = SystemClock.elapsedRealtime() + repeatInterval;
        Log.d(LOG_TAG, "repeat interval: " + repeatInterval);
        if (mAlarmManager != null) {
            mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, repeatInterval, alarmPendingIntent);
            Log.d(LOG_TAG, "notif. set at: " + milisToTime(triggerTime));
        }
    }

    private void cancelAlarmPendingIntent() {
        Intent alarmIntent = new Intent(getContext(), AlarmReceiver.class);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(getContext(), NOTIFICATION_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mAlarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        if (mAlarmManager!=null) {
            mAlarmManager.cancel(alarmPendingIntent);
        }
    }

    //only for debug
    private String milisToTime(long milis) {
        Date date = new Date(milis);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+02:00")); // timezone is wrong
        return formatter.format(date);
    }
}
