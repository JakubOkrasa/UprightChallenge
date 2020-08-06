package com.example.uprightchallenge;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
// todo #bug notif toggle is on at the beginning (sometimes?)
// todo #later show number of daily count in notification
public class MainActivity extends AppCompatActivity {
    private static String CORRECT_POSTURE_COUNT;
    private NotificationManager mNotifyManager;
    private static final int NOTIFICATION_ID = 0;
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private CorrectPostureReceiver mCorrectPostureReceiver = new CorrectPostureReceiver(this);
    private static final String POSTURE_YES_ACTION = BuildConfig.APPLICATION_ID + ".POSTURE_YES_ACTION"; //the same line of code in AlarmReceiver (!)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();
        ToggleButton notifToggle = findViewById(R.id.notify_toggle);
        final AlarmManager mAlarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        notifToggle.setChecked(PendingIntent.getBroadcast(this, NOTIFICATION_ID, alarmIntent, PendingIntent.FLAG_NO_CREATE) != null); // check if notifications were turned on before the new MainActivity was stopped
        final PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        registerReceiver(mCorrectPostureReceiver, new IntentFilter(POSTURE_YES_ACTION));
        notifToggle.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        String toastMessage = "error: AlarmManager is null";
                        if(isChecked) {
//                          long repeatInterval = AlarmManager.INTERVAL_HALF_HOUR;
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

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);  //should be commmented?
        mCorrectPostureReceiver.getTxtCount().setText(savedInstanceState.getString(CORRECT_POSTURE_COUNT));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(CORRECT_POSTURE_COUNT, String.valueOf(mCorrectPostureReceiver.getCount()));
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mCorrectPostureReceiver);
        super.onDestroy();
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
