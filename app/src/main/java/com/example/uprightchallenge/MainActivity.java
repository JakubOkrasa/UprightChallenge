package com.example.uprightchallenge;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private NotificationManager mNotifyManager;
    private static final int NOTIFICATION_ID = 0;
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();
        ToggleButton notifToggle = findViewById(R.id.notify_toggle);
        final AlarmManager mAlarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        final PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notifToggle.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        String toastMessage = "error: AlarmManager is null";
                        if(isChecked) {
//                          long repeatInterval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
                            long repeatInterval = 30000; //short interval only for debug
                            long triggerTime = SystemClock.elapsedRealtime() + repeatInterval;
                            if(mAlarmManager!=null) {
                                mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, repeatInterval, alarmPendingIntent);
                                toastMessage = "Upright notifications on.";
                            }
                        }
                        else {
                            mNotifyManager.cancelAll();
                            if (mAlarmManager!=null) {
                                mAlarmManager.cancel(alarmPendingIntent);
                            }
                            toastMessage = "Upright notifications off.";
                        }
                        Toast.makeText(MainActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void createNotificationChannel() {
        mNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
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
}
