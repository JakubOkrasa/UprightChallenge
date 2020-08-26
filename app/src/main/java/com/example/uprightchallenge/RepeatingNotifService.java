package com.example.uprightchallenge;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class RepeatingNotifService extends Service {
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private NotificationManager mNotifyManager;
    private final String LOG_TAG = RepeatingNotifService.class.getSimpleName();
    private SharedPreferences preferences;
    private String sharedPrefsFile = BuildConfig.APPLICATION_ID;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        preferences = getBaseContext().getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE); //todo can be context instead of getActivity() ?
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

    void cancelNotifications() {
        mNotifyManager.cancelAll(); // cancel existing notifications
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
