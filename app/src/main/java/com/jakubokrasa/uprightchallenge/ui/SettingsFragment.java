package com.jakubokrasa.uprightchallenge.ui;

import android.app.AlarmManager;
import android.app.NotificationManager;
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

import com.jakubokrasa.uprightchallenge.data.PostureStatDatabase;
import com.jakubokrasa.uprightchallenge.receiver.NotifAlarmReceiver;
import com.jakubokrasa.uprightchallenge.BuildConfig;
import com.jakubokrasa.uprightchallenge.R;
import com.jakubokrasa.uprightchallenge.service.RepeatingNotifService;
import com.jakubokrasa.uprightchallenge.receiver.ResetAlarmReceiver;

import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    private SharedPreferences preferences;
    public static final String sharedPrefsFile = BuildConfig.APPLICATION_ID;
    private SharedPreferences.Editor prefsEditor;
    private final String LOG_TAG = SettingsFragment.class.getSimpleName();
    public static final int NOTIFICATION_ID = 0;
    public static final int RESET_ALARM_ID = 1;
    public static final int NIGHT_HOURS_ALARM_ID = 2;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        Intent startServiceIntent = new Intent(getContext(), RepeatingNotifService.class);
        requireContext().startService(startServiceIntent);

        setPreferencesFromResource(R.xml.preferences, rootKey);

        if(getActivity().getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE)!=null) {
            preferences = getActivity().getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE);
        }

        SwitchPreferenceCompat notificationSwitch = findPreference("pref_key_switch_notifications");
        ListPreference intervalListPref = findPreference("pref_key_interval"); //todo ListPreference is created twice: here and inner class. Maybe it is possible to create one

        if(notificationSwitch!=null) {
            notificationSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object notificationsOnObject) {
                    boolean notificationsOn = (Boolean) notificationsOnObject;
                    ListPreference intervalListPref = findPreference("pref_key_interval");
                    if(notificationsOn) {
                        if(preferences!=null) {
                            setAlarmPendingIntent();
                            setResetPendingIntent();
                        }
                    }
                    else {
                        ((NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
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
          intervalListPref.setEnabled(preferences.getBoolean("pref_key_switch_notifications", false));

        //only for tests
        Preference populateWithSampleDataBtnPref = findPreference("pref_key_populate");
        if(populateWithSampleDataBtnPref!=null) {
            populateWithSampleDataBtnPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //Although it's not a good practice to call Database layer method from UI,
                    //it was done only for app testing/debugging purposes and will be removed in the official
                    // version of the app TODO remove in official version
                    PostureStatDatabase.getDatabase(getContext()).populateDbWithSampleData();
                    return true;
                }
            });
        }

    }

    private void setAlarmPendingIntent() {
        AlarmManager mAlarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(getContext(), NotifAlarmReceiver.class);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(getContext(), NOTIFICATION_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT); //todo rename to NotifAlarmPendingIntent, similarly alarmIntent
        long repeatInterval = preferences.getLong("pref_key_interval", AlarmManager.INTERVAL_HALF_HOUR);
        long triggerTime = SystemClock.elapsedRealtime() + repeatInterval;
        Log.d(LOG_TAG, "repeat interval: " + repeatInterval);
        if (mAlarmManager != null) {
            mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, repeatInterval, alarmPendingIntent);
        }
    }

    private void cancelAlarmPendingIntent() {
        Intent alarmIntent = new Intent(getContext(), NotifAlarmReceiver.class);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(getContext(), NOTIFICATION_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mAlarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        if (mAlarmManager!=null) {
            mAlarmManager.cancel(alarmPendingIntent);
        }
    }

    // Set the alarm to start at approximately 1:00 a.m. The alarm will be used to reset counters every night and save results in database
    private void setResetPendingIntent() { // TODO: rename/extract this and similar methods in this class. There is not only pending intent set.
        AlarmManager mAlarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0); // set calendar hour to 0 a.m.
        calendar.set(Calendar.MINUTE, 0);
        calendar.add(Calendar.DATE, 1);
//        calendar.set(Calendar.MINUTE, 29); //for debug only
        Intent resetAlarmIntent = new Intent(getContext(), ResetAlarmReceiver.class);
        PendingIntent resetAlarmPendingIntent = PendingIntent.getBroadcast(getContext(), RESET_ALARM_ID, resetAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, resetAlarmPendingIntent);
    }

    private void setNightHoursAlarm(boolean nightHoursTurnedON) {
        Intent nightHoursIntent = new Intent(getContext(), NightHoursReceiver.class);
        PendingIntent nightHoursPendingIntent = PendingIntent.getBroadcast(getContext(), NIGHT_HOURS_ALARM_ID, nightHoursIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager mAlarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if(nightHoursTurnedON) {
            calendar.set(Calendar.HOUR, 21);
            calendar.set(Calendar.MINUTE, 0);
        } else {
            calendar.set(Calendar.HOUR, 7);
            calendar.set(Calendar.MINUTE, 30);
        }
        mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, nightHoursPendingIntent);
    }
}
