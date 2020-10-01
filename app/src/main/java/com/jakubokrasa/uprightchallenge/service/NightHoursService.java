package com.jakubokrasa.uprightchallenge.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.jakubokrasa.uprightchallenge.BuildConfig;
import com.jakubokrasa.uprightchallenge.RepeatingNotifHelper;

import static com.jakubokrasa.uprightchallenge.ui.SettingsFragment.NIGHT_HOURS_OFF_ACTION; // TODO: 10/1/2020 can it be imported from UI component (which sometimes is destroyed and does not exist
import static com.jakubokrasa.uprightchallenge.ui.SettingsFragment.NIGHT_HOURS_ON_ACTION;
import static com.jakubokrasa.uprightchallenge.ui.SettingsFragment.sharedPrefsFile;

public class NightHoursService extends Service {
    private final String LOG_TAG = NightHoursService.class.getSimpleName();
    private RepeatingNotifHelper notifHelper;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter nightHoursFilter = new IntentFilter();
        nightHoursFilter.addAction(NIGHT_HOURS_ON_ACTION);
        nightHoursFilter.addAction(NIGHT_HOURS_OFF_ACTION);
        registerReceiver(nightHoursReceiver, nightHoursFilter);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(nightHoursReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    BroadcastReceiver nightHoursReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(notifHelper==null) { notifHelper = new RepeatingNotifHelper(context); }
            SharedPreferences preferences = context.getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE);
            SharedPreferences.Editor prefEditor = preferences.edit();
            Log.d(LOG_TAG, "NIGHT_RECEIVER: received");
            if (intent.getAction().equals(NIGHT_HOURS_ON_ACTION)) {
                Log.d(LOG_TAG, "NIGHT_RECEIVER: action on night hours received");
                prefEditor.putBoolean("pref_key_switch_notifications", false);
                notifHelper.turnOffNotifications();
            }
            else if(intent.getAction().equals(NIGHT_HOURS_OFF_ACTION)) {
                Log.d(LOG_TAG, "NIGHT_RECEIVER: action off night hours received");
                prefEditor.putBoolean("pref_key_switch_notifications", true);
                notifHelper.turnOnNotifications();
            }
            prefEditor.apply();
        }
    };
}
