package com.jakubokrasa.uprightchallenge.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.jakubokrasa.uprightchallenge.ui.SettingsFragment.SCHEDULED_NOTIF_ON_ACTION;
import static com.jakubokrasa.uprightchallenge.ui.SettingsFragment.SCHEDULED_NOTIF_OFF_ACTION;
import static com.jakubokrasa.uprightchallenge.ui.SettingsFragment.NOTIFICATION_ID;
import static com.jakubokrasa.uprightchallenge.ui.SettingsFragment.sharedPrefsFile;

public class RepeatingNotifService extends Service { // TODO: 10/1/2020 rename to PostureNotifService
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private NotificationManager mNotifyManager;
    private final String LOG_TAG = RepeatingNotifService.class.getSimpleName();
    private static final String PREF_KEY_GOOD_POSTURE_COUNT = "good_posture_count";
    private static final String PREF_KEY_BAD_POSTURE_COUNT = "bad_posture_count";
    public static final String GOOD_POSTURE_ACTION = BuildConfig.APPLICATION_ID + ".GOOD_POSTURE_ACTION";
    public static final String BAD_POSTURE_ACTION = BuildConfig.APPLICATION_ID + ".BAD_POSTURE_ACTION";
    private RepeatingNotifHelper notifHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        IntentFilter filter = new IntentFilter();
        filter.addAction(SCHEDULED_NOTIF_OFF_ACTION);
        filter.addAction(SCHEDULED_NOTIF_ON_ACTION);
        registerReceiver(notifOnTimeReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handlePostureIntent(intent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(notifOnTimeReceiver);
        super.onDestroy();
    }

    private void createNotificationChannel() {
        mNotifyManager = (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        long[] vibPattern = {0, 200, 200, 200, 200, 200}; //{delay1, vibDuration1, delay2, vibDuration2...}
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handlePostureIntent(Intent intent) {
        if (intent.getAction()==null) {return;}
        final String sharedPrefFile = BuildConfig.APPLICATION_ID;
        SharedPreferences preferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = preferences.edit();
        mNotifyManager.cancel(NOTIFICATION_ID);
        if (intent.getAction().equals(GOOD_POSTURE_ACTION)) {
            Log.d(LOG_TAG, "yes option clicked");
            mNotifyManager.cancel(NOTIFICATION_ID);
            int count = preferences.getInt(PREF_KEY_GOOD_POSTURE_COUNT, 0);
            prefEditor.putInt(PREF_KEY_GOOD_POSTURE_COUNT, ++count).apply();
        }
        else if (intent.getAction().equals(BAD_POSTURE_ACTION)) {
            Log.d(LOG_TAG, "no option clicked");
            mNotifyManager.cancel(NOTIFICATION_ID);
            int count = preferences.getInt(PREF_KEY_BAD_POSTURE_COUNT, 0);
            prefEditor.putInt(PREF_KEY_BAD_POSTURE_COUNT, ++count).apply();
        }
    }

    // receives when is time to switch on/off notifications (user chooses time when he will be given notifications, usually notifications at night are unwanted)
    BroadcastReceiver notifOnTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(notifHelper==null) {
                notifHelper = new RepeatingNotifHelper(context); }
            SharedPreferences preferences = context.getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE);
            SharedPreferences.Editor prefEditor = preferences.edit();
            if (intent.getAction().equals(SCHEDULED_NOTIF_OFF_ACTION)) {
                Log.d(LOG_TAG, getTime() + " action notif OFF received");
                prefEditor.putBoolean("pref_key_switch_notifications", false);
                notifHelper.cancelAlarmPendingIntent();
                mNotifyManager.cancelAll();
            }
            else if(intent.getAction().equals(SCHEDULED_NOTIF_ON_ACTION)) {
                Log.d(LOG_TAG, getTime() + " action notif ON received");
                prefEditor.putBoolean("pref_key_switch_notifications", true);
                notifHelper.setAlarmPendingIntent();
            }
            prefEditor.apply();
        }
    };

    //for debugging only
    private String getTime() {
        return new SimpleDateFormat("HH:mm:ss", Locale.ROOT).format(Calendar.getInstance().getTime());
    }
}
