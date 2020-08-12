package com.example.uprightchallenge;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
// todo #bug notif toggle is on at the beginning (sometimes?)
// todo #later show number of daily count in notification

// todo #note when I added if(savedInstanceState != null) {..} and onRestore lines, AND in the app click BACK button to check the right count number, nothing happens
public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String CORRECT_POSTURE_COUNT_KEY = "count";
    private NotificationManager mNotifyManager;
    private static final int NOTIFICATION_ID = 0;
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private CorrectPostureReceiver mCorrectPostureReceiver = new CorrectPostureReceiver(this);
    static final String POSTURE_YES_ACTION = BuildConfig.APPLICATION_ID + ".POSTURE_YES_ACTION";
    private int mCorrectPostureCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        ToggleButton mNotifToggle = findViewById(R.id.notify_toggle);
        TextView mCorrectPostureTextView = findViewById(R.id.txt_count);

        //set alarmPendingIntent to deliver repeating notifications
        final AlarmManager mAlarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        mNotifToggle.setChecked(PendingIntent.getBroadcast(this, NOTIFICATION_ID, alarmIntent, PendingIntent.FLAG_NO_CREATE) != null); // check if notifications were turned on before the new MainActivity was stopped
        final PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        registerReceiver(mCorrectPostureReceiver, new IntentFilter(POSTURE_YES_ACTION));

        mNotifToggle.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        String toastMessage = "error: AlarmManager is null";
                        if(isChecked) {
                            long repeatInterval;
                            repeatInterval = 30000; //short interval only for debug
//                            if(Build.FINGERPRINT.startsWith("google/sdk_gphone_x86/generic")) { todo uncomment before commit
//                                repeatInterval = 30000; //short interval only for debug
//                            }
//                            else {
//                                repeatInterval = AlarmManager.INTERVAL_HALF_HOUR;
//                            }
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

        if(savedInstanceState != null) {
            int count = savedInstanceState.getInt(CORRECT_POSTURE_COUNT_KEY);
            if (count != 0) {
                mCorrectPostureTextView.setText(String.format("%s", count));
            }

        }

        Log.d(LOG_TAG, "A: created");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "A: started");

    }

    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "A: stopped");
        super.onStop();
    }



    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);  //should be commmented?
        //mCorrectPostureReceiver.getTxtCount().setText(savedInstanceState.getString(CORRECT_POSTURE_COUNT_STRING)); // todo check if needed!!
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
//        int receivedCount = mCorrectPostureReceiver.getCount();
//        if (receivedCount!=0) {
            outState.putInt(CORRECT_POSTURE_COUNT_KEY, mCorrectPostureReceiver.getCount());
//        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mCorrectPostureReceiver);
        Log.d(LOG_TAG, "A: destroying");
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
