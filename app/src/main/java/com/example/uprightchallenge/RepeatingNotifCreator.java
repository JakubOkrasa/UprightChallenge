package com.example.uprightchallenge;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class RepeatingNotifCreator extends Service {
    private static final int NOTIFICATION_ID = 0;
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private NotificationManager mNotifyManager;
    private AlarmManager mAlarmManager;
    private PendingIntent alarmPendingIntent;
    private final String LOG_TAG = RepeatingNotifCreator.class.getSimpleName();
    private SharedPreferences preferences;
    private String sharedPrefsFile = BuildConfig.APPLICATION_ID;
    private Context context;

    public RepeatingNotifCreator(Context context) {
        this.context = context;
        createNotificationChannel();
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        preferences = context.getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE); //todo can be context instead of getActivity() ?
    }

    private void createNotificationChannel() {
        mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
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

    void setAlarmPendingIntent() { //todo static?
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmPendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT); //todo check if cancelling always onCreate doesn't cancel needed intents
        long repeatInterval = preferences.getLong("pref_key_interval", 1800000); //1800000ms = 30min
//        if (Build.FINGERPRINT.startsWith("google/sdk_gphone_x86/generic") || Build.FINGERPRINT.startsWith("samsung")) { repeatInterval = 30000; }//short interval only for debug
        long triggerTime = SystemClock.elapsedRealtime() + repeatInterval;
        Log.d(LOG_TAG, "repeat interval: " + repeatInterval);
        if (mAlarmManager != null) {
            mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, repeatInterval, alarmPendingIntent);
            Log.d(LOG_TAG, "notif. set at: " + milisToTime(triggerTime));
        }
    }

    void cancelAlarmPendingIntent() { //todo static?
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

    void cancelNotifications() {
        mNotifyManager.cancelAll(); // cancel existing notifications
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
