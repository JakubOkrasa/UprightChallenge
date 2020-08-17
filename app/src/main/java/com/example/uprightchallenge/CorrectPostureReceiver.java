package com.example.uprightchallenge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.TextView;

import static android.content.Context.MODE_PRIVATE;

class CorrectPostureReceiver extends BroadcastReceiver {

    private int mCount = 0;
    private TextView mTxtCount;
    private final String LOG_TAG = CorrectPostureReceiver.class.getSimpleName();
    private MainActivity mainActivity;
    private static final String KEY_YES_POSTURE_COUNT = "yes_posture_count";

    public CorrectPostureReceiver(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "yes option clicked");
        mTxtCount = mainActivity.findViewById(R.id.txt_count); //todo mCount initialize from prefs, mayby mainActivity not necessary
        if (mTxtCount != null) {
            mCount++;
//            mTxtCount.setText(Integer.toString(mCount));
        }

        final String sharedPrefFile = BuildConfig.APPLICATION_ID;
        SharedPreferences preferences =  context.getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = preferences.edit();
        prefEditor.putInt(KEY_YES_POSTURE_COUNT, mCount).apply();
    }

    public int getCount() {
        return mCount;
    }
    public TextView getTxtCount() {
        return mTxtCount;
    }
}