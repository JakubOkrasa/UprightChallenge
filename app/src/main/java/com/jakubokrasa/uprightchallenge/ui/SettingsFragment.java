package com.jakubokrasa.uprightchallenge.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import android.util.Log;

import com.dr1009.app.chronodialogpreference.ChronoPreferenceFragment;
import com.dr1009.app.chronodialogpreference.TimeDialogPreference;
import com.jakubokrasa.uprightchallenge.RepeatingNotifHelper;
import com.jakubokrasa.uprightchallenge.data.PostureStatDatabase;
import com.jakubokrasa.uprightchallenge.BuildConfig;
import com.jakubokrasa.uprightchallenge.R;
import com.jakubokrasa.uprightchallenge.service.RepeatingNotifService;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends ChronoPreferenceFragment {

    public static final String SCHEDULED_NOTIF_OFF_ACTION = BuildConfig.APPLICATION_ID + ".SCHEDULED_NOTIF_OFF_ACTION";
    public static final String SCHEDULED_NOTIF_ON_ACTION = BuildConfig.APPLICATION_ID + ".SCHEDULED_NOTIF_ON_ACTION";
    private SharedPreferences preferences;
    public static final String sharedPrefsFile = BuildConfig.APPLICATION_ID;
    private SharedPreferences.Editor prefsEditor;
    private final String LOG_TAG = SettingsFragment.class.getSimpleName();
    public static final int NOTIFICATION_ID = 0;
    public static final int RESET_ALARM_ID = 1;
    public static final int NOTIF_ON_TIME_ALARM = 2;
    RepeatingNotifHelper notifHelper;

    @Override
    public void onStart() {
        super.onStart();
        prefsEditor = preferences.edit();
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
        final TimeDialogPreference timeNotifBeginPref = findPreference(requireContext().getResources().getString(R.string.pref_key_notif_on_time));
        final TimeDialogPreference timeNotifEndPref = findPreference(requireContext().getResources().getString(R.string.pref_key_notif_off_time));

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
                            notifHelper.setNotifOnTimeRange();
                        }
                    }
                    else {
                        Log.d(LOG_TAG, "notifications off");
                        notifHelper.turnOffNotifications();
                    }
                    // save changes to SharedPreferences
                    prefsEditor.putBoolean(preference.getKey(), notificationsOn).apply();
                    intervalListPref.setEnabled(notificationsOn); //does it can be removed? >> final field outside? Test timeNotifPrefs
                    timeNotifBeginPref.setEnabled(notificationsOn);
                    timeNotifEndPref.setEnabled(notificationsOn);
                    return true;
                }
            });
        }

        if(timeNotifBeginPref!=null) {
            timeNotifBeginPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String time = (String) newValue;
                    prefsEditor.putString(getContext().getResources().getString(R.string.pref_key_notif_on_time), time).apply();
                    notifHelper.setNotifOnTimeRange();
                    return true;
                }
            });
        }

        if(timeNotifEndPref!=null) {
            timeNotifEndPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String time = (String) newValue;
                    prefsEditor.putString(getContext().getResources().getString(R.string.pref_key_notif_off_time), time).apply();
                    notifHelper.setNotifOnTimeRange();
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
                    prefsEditor.putLong(preference.getKey(), Long.parseLong(interval)).apply();
                    notifHelper.setAlarmPendingIntent();
                    return true;
                }
            });
        }

        intervalListPref.setEnabled(preferences.getBoolean("pref_key_switch_notifications", false));
        timeNotifBeginPref.setEnabled(preferences.getBoolean("pref_key_switch_notifications", false));
        timeNotifEndPref.setEnabled(preferences.getBoolean("pref_key_switch_notifications", false));

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

    private void enablePrefsNotifRelated() {
        
    }


}
