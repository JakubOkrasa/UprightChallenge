package com.example.uprightchallenge;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    CountDownTimer timer;
    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) { }

            @Override
            public void onFinish() {
                vibrate();
            }
        };
    }

    public void runTimer(View view) {
        timer.start();
    }

    private void vibrate() {
        if (vibrator.hasVibrator()) {
            Log.i("vibrator", "The device has a vibrator :)");
            vibrator.vibrate(1000);
        }
        else {
            Log.e("vibrator", "The device no vibrator");
        }
    }
}
