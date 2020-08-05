package com.example.uprightchallenge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class YesPostureReceiver extends BroadcastReceiver {

    public YesPostureReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(YesPostureReceiver.class.getSimpleName(), "yes option clicked");
    }
}
