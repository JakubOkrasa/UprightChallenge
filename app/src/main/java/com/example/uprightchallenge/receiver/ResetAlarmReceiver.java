package com.example.uprightchallenge.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.uprightchallenge.BuildConfig;
import com.example.uprightchallenge.data.PostureStat;
import com.example.uprightchallenge.data.PostureStatRepository;

public class ResetAlarmReceiver extends BroadcastReceiver {
    private PostureStatRepository mRepository;
    public static final String sharedPrefsFile = BuildConfig.APPLICATION_ID;
    private static final String PREF_KEY_GOOD_POSTURE_COUNT = "good_posture_count";
    private static final String PREF_KEY_BAD_POSTURE_COUNT = "bad_posture_count";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = context.getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = preferences.edit();

        mRepository=PostureStatRepository.getRepository(context);
        mRepository.insert(new PostureStat(
                0,
                preferences.getInt(PREF_KEY_GOOD_POSTURE_COUNT, 0),
                preferences.getInt(PREF_KEY_BAD_POSTURE_COUNT, 0)));

        // reset posture counters
        prefsEditor.putInt(PREF_KEY_BAD_POSTURE_COUNT, 0);
        prefsEditor.putInt(PREF_KEY_GOOD_POSTURE_COUNT, 0);
        prefsEditor.apply();

    }
}
