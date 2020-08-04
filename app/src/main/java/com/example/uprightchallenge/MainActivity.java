package com.example.uprightchallenge;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.os.Vibrator;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    //CountDownTimer timer;
   // Vibrator vibrator;
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
        final Vibrator mVibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        final PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notifToggle.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        String toastMessage = "error: AlarmManager is null";
                        if(isChecked) {
//                            long repeatInterval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
                            long repeatInterval = 30000;
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
//        ctx = getApplicationContext();
//        durationSpinner = (Spinner) findViewById(R.id.duration_spinner);
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.duration_array, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        durationSpinner.setAdapter(adapter);
//        vibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
//        timer = new CountDownTimer(10000, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//
//            }
//
//            @Override
//            public void onFinish() {
//                Button buttonVibe = findViewById(R.id.btnvibe);
//                buttonVibe.setText("vibing");
//                //startVibrate();
//                timer.start();
//            }
//        };

    }

    private void createNotificationChannel() {
        mNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        long[] vibPattern = {0, 100, 200, 100, 200};
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    PRIMARY_CHANNEL_ID,
                    "Check posture notification",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(vibPattern);
            notificationChannel.setDescription("notification check posture");
            mNotifyManager.createNotificationChannel(notificationChannel);
        }
    }

    /*
    public void runTimer(View view) {
        timer.start();
    }

    private void startVibrate() {
        vibrator.vibrate(1000);
    }
    */


}
