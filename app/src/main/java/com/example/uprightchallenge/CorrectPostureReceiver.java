package com.example.uprightchallenge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

import com.example.uprightchallenge.MainActivity;
import com.example.uprightchallenge.R;

class CorrectPostureReceiver extends BroadcastReceiver {

    private int mCount = 0;
    private TextView mTxtCount;
    private final String LOG_TAG = CorrectPostureReceiver.class.getSimpleName();
    private MainActivity mainActivity;
    public CorrectPostureReceiver(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "yes option clicked");
        mTxtCount = mainActivity.findViewById(R.id.txt_count);
        if (mTxtCount != null) {
            mTxtCount.setText(Integer.toString(mCount++));
        }
    }

    public int getCount() {
        return mCount;
    }

    public TextView getTxtCount() {
        return mTxtCount;
    }
}