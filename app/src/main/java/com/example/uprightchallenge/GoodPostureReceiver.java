package com.example.uprightchallenge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.TextView;

import static android.content.Context.MODE_PRIVATE;

class GoodPostureReceiver extends BroadcastReceiver {

    private int mCount = 0;
    private TextView mTxtCount;
    private final String LOG_TAG = GoodPostureReceiver.class.getSimpleName();
    private static final String PREF_KEY_GOOD_POSTURE_COUNT = "good_posture_count";
    private MainActivity mainActivity;

    public GoodPostureReceiver(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "yes option clicked");
        mCount++;
        final String sharedPrefFile = BuildConfig.APPLICATION_ID;
        SharedPreferences preferences =  context.getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = preferences.edit();
        prefEditor.putInt(PREF_KEY_GOOD_POSTURE_COUNT, mCount).apply();
        mTxtCount = mainActivity.findViewById(R.id.txt_good_posture_count);
        if (mTxtCount != null) {
            mTxtCount.setText(Integer.toString(mCount));
        }
    }
}