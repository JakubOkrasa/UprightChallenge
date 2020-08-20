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
        //set alarmPendingIntent to deliver repeating notifications
        mAlarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(getContext(), AlarmReceiver.class);
//        alarmPendingIntent = PendingIntent.getBroadcast(getContext(), NOTIFICATION_ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmPendingIntent = PendingIntent.getBroadcast(getContext(), NOTIFICATION_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT); //todo check if cancelling always onCreate doesn't cancel needed intents

        createNotificationChannel();
        SwitchPreferenceCompat notificationSwitch = findPreference("switch_notifications");

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
                    return true;
                }
            });
        }

        ListPreference intervalListPref = findPreference("list_intervals");

        if(intervalListPref!=null) {
            intervalListPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String interval =(String) newValue;
                    prefsEditor = preferences.edit();
                    prefsEditor.putLong(preference.getKey(), Long.parseLong(interval)).apply();
                    Log.d(LOG_TAG, "interval set: " + Long.parseLong(interval));
                    updateAlarmPendingIntent();
                    return true;
                }
            });
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

    private void updateAlarmPendingIntent() {
        cancelAlarmPendingIntent();
        setAlarmPendingIntent();
    }

    private void setAlarmPendingIntent() {
        long repeatInterval = preferences.getLong(getResources().getString(R.string.interval_list_pref), 1800); //1800ms = 30min
//        if (Build.FINGERPRINT.startsWith("google/sdk_gphone_x86/generic") || Build.FINGERPRINT.startsWith("samsung")) { repeatInterval = 30000; }//short interval only for debug
        long triggerTime = SystemClock.elapsedRealtime() + repeatInterval;
        Log.d(LOG_TAG, "repeat interval: " + repeatInterval);
        if (mAlarmManager != null) {
            mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, repeatInterval, alarmPendingIntent);
        }
    }

    private void cancelAlarmPendingIntent() {
        if (mAlarmManager!=null) {
            mAlarmManager.cancel(alarmPendingIntent);
        }
    }
}
