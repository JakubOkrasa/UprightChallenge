package com.example.uprightchallenge;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static android.content.Context.ALARM_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */

/*
//todo #note :
    After turning on notif, (default is 30 minutes interval) notifications appear very often, maybe even less than 30 seconds.
 */
public class SettingsFragment extends PreferenceFragmentCompat {
    private static final int NOTIFICATION_ID = 0;
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private NotificationManager mNotifyManager;
    private String sharedPrefsFile = BuildConfig.APPLICATION_ID;
    private SharedPreferences preferences;
    private AlarmManager mAlarmManager;
    private PendingIntent alarmPendingIntent;
    private SharedPreferences.Editor prefsEditor;
//    private final int KEY_PREF_INTERVAL = R.string.key_pref_interval;
    private final String LOG_TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        if(getActivity().getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE)!=null) {
            preferences = getActivity().getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE);
        }

        SwitchPreferenceCompat notificationSwitch = findPreference("switch_notifications");
        final ListPreference intervalListPref = findPreference("key_pref_interval"); //todo not sure if it can be final
//        ListPreference intervalListPref = findPreference(getResources().getString(R.string.interval_list_pref)); // this way (getResources()) doesn't work

        //set alarmPendingIntent to deliver repeating notifications
        mAlarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
//        alarmPendingIntent = PendingIntent.getBroadcast(getContext(), NOTIFICATION_ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        createNotificationChannel();
        if(notificationSwitch!=null) {
            notificationSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object notificationsOnObject) {
                    boolean notificationsOn = (Boolean) notificationsOnObject;
                    if(notificationsOn) {
                        if(preferences!=null) {
                            setAlarmPendingIntent();
                        }
                        else {  //todo this should be avoided with setting defauld prefs ealier ( this 'if .. else' is only for debug)
                            Log.e(LOG_TAG, "AlarmPendingIntent not set (prefs were null)");
                        }
                    }
                    else {
                        mNotifyManager.cancelAll(); // cancel existing notifications
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
    }

    private void createNotificationChannel() {
        mNotifyManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        long[] vibPattern = {0, 200, 200, 200, 200, 200}; //{delay1, vibDuration1, delay2, vibDuration2...}
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    PRIMARY_CHANNEL_ID,
                    "Check posture notification",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("notification check posture");
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(vibPattern);
            mNotifyManager.createNotificationChannel(notificationChannel);
        }
    }

    private void setAlarmPendingIntent() {
        Intent alarmIntent = new Intent(getContext(), AlarmReceiver.class);
        alarmPendingIntent = PendingIntent.getBroadcast(getContext(), NOTIFICATION_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT); //todo check if cancelling always onCreate doesn't cancel needed intents
        long repeatInterval = preferences.getLong("key_pref_interval", 1800000); //1800000ms = 30min
//        if (Build.FINGERPRINT.startsWith("google/sdk_gphone_x86/generic") || Build.FINGERPRINT.startsWith("samsung")) { repeatInterval = 30000; }//short interval only for debug
        long triggerTime = SystemClock.elapsedRealtime() + repeatInterval;
        Log.d(LOG_TAG, "repeat interval: " + repeatInterval);
        if (mAlarmManager != null) {
            mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, repeatInterval, alarmPendingIntent);
            Log.d(LOG_TAG, "notif. set at: " + milisToTime(triggerTime));
        }
    }

    private void cancelAlarmPendingIntent() {
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
