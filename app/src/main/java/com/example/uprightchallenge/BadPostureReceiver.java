package com.example.uprightchallenge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.TextView;

import static android.content.Context.MODE_PRIVATE;

public class BadPostureReceiver extends BroadcastReceiver {
    private int mCount = 0;
    private TextView mTxtCount;
    private final String LOG_TAG = GoodPostureReceiver.class.getSimpleName();
    private static final String KEY_NO_POSTURE_COUNT = "no_posture_count";
    private MainActivity mainActivity;

    public BadPostureReceiver(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "no option clicked");
        mCount++;
        final String sharedPrefFile = BuildConfig.APPLICATION_ID;
        SharedPreferences preferences =  context.getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = preferences.edit();
        prefEditor.putInt(KEY_NO_POSTURE_COUNT, mCount).apply();
        mTxtCount = mainActivity.findViewById(R.id.txt_bad_posture_count);
        if (mTxtCount != null) {
            mTxtCount.setText(Integer.toString(mCount));
        }
    }
}
