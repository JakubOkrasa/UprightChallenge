package com.jakubokrasa.uprightchallenge.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.jakubokrasa.uprightchallenge.BuildConfig;
import com.jakubokrasa.uprightchallenge.data.PostureStat;
import com.jakubokrasa.uprightchallenge.data.PostureStatRepository;

public class ResetAlarmReceiver extends BroadcastReceiver {
    public static final String sharedPrefsFile = BuildConfig.APPLICATION_ID;
    private static final String PREF_KEY_GOOD_POSTURE_COUNT = "good_posture_count";
    private static final String PREF_KEY_BAD_POSTURE_COUNT = "bad_posture_count";
    public static final String LOG_TAG = ResetAlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = context.getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = preferences.edit();

        // save stats to repository
        PostureStatRepository mRepository = PostureStatRepository.getRepository(context);
        mRepository.insert(new PostureStat(
                0,
                preferences.getInt(PREF_KEY_GOOD_POSTURE_COUNT, 0),
                preferences.getInt(PREF_KEY_BAD_POSTURE_COUNT, 0)));

        // reset posture counters
        prefsEditor.putInt(PREF_KEY_BAD_POSTURE_COUNT, 0);
        prefsEditor.putInt(PREF_KEY_GOOD_POSTURE_COUNT, 0);
        prefsEditor.apply();
        Log.d(LOG_TAG, "new PostureStat saved");
    }
}
