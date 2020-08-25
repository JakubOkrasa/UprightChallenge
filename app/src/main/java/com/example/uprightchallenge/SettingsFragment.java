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
import java.util.Objects;
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

    private SharedPreferences preferences;
    private String sharedPrefsFile = BuildConfig.APPLICATION_ID;
    private SharedPreferences.Editor prefsEditor;
    private RepeatingNotifCreator repeatingNotifCreator;
    private final String LOG_TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        repeatingNotifCreator = new RepeatingNotifCreator(requireContext());
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
                            repeatingNotifCreator.setAlarmPendingIntent();
                        }
                        else {  //todo this should be avoided with setting defauld prefs ealier ( this 'if .. else' is only for debug)
                            Log.e(LOG_TAG, "AlarmPendingIntent not set (prefs were null)");
                        }
                    }
                    else {
                        repeatingNotifCreator.cancelNotifications();
                        repeatingNotifCreator.cancelAlarmPendingIntent(); // cancel repeating intent messages for AlarmReceiver
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
                    repeatingNotifCreator.setAlarmPendingIntent();
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


}
