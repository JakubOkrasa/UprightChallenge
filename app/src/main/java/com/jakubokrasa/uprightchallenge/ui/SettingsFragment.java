package com.jakubokrasa.uprightchallenge.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import android.util.Log;

import com.dr1009.app.chronodialogpreference.ChronoPreferenceFragment;
import com.jakubokrasa.uprightchallenge.RepeatingNotifHelper;
import com.jakubokrasa.uprightchallenge.data.PostureStatDatabase;
import com.jakubokrasa.uprightchallenge.BuildConfig;
import com.jakubokrasa.uprightchallenge.R;
import com.jakubokrasa.uprightchallenge.service.RepeatingNotifService;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends ChronoPreferenceFragment {

    public static final String NIGHT_HOURS_ON_ACTION = BuildConfig.APPLICATION_ID + ".NIGHT_HOURS_ON_ACTION";
    public static final String NIGHT_HOURS_OFF_ACTION = BuildConfig.APPLICATION_ID + ".NIGHT_HOURS_OFF_ACTION";
    private SharedPreferences preferences;
    public static final String sharedPrefsFile = BuildConfig.APPLICATION_ID;
    private SharedPreferences.Editor prefsEditor;
    private final String LOG_TAG = SettingsFragment.class.getSimpleName();
    public static final int NOTIFICATION_ID = 0;
    public static final int RESET_ALARM_ID = 1;
    public static final int NIGHT_HOURS_ALARM_ID = 2;
    RepeatingNotifHelper notifHelper;

    @Override
    public void onStart() {
        super.onStart();
        SwitchPreferenceCompat notificationSwitch = findPreference("pref_key_switch_notifications");
        if(notificationSwitch!=null) {
            notificationSwitch.setChecked(preferences.getBoolean("pref_key_switch_notifications", false));
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        Intent startServiceIntent = new Intent(getContext(), RepeatingNotifService.class);
        requireContext().startService(startServiceIntent);

        notifHelper = new RepeatingNotifHelper(requireContext());

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
                    Log.d(LOG_TAG, "notif switch preference change");
                    boolean notificationsOn = (Boolean) notificationsOnObject;
                    ListPreference intervalListPref = findPreference("pref_key_interval");
                    if(notificationsOn) {
                        Log.d(LOG_TAG, "notifications on");
                        if(preferences!=null) {
                            notifHelper.setAlarmPendingIntent();
                            notifHelper.setResetPendingIntent();
                            notifHelper.setNightHoursAlarm();
                        }
                    }
                    else {
                        Log.d(LOG_TAG, "notifications off");
                        notifHelper.turnOffNotifications();
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
                    notifHelper.setAlarmPendingIntent();
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


}
