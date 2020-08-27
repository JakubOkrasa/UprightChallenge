package com.example.uprightchallenge;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import static com.example.uprightchallenge.SettingsFragment.sharedPrefsFile;
// todo #later show number of daily count in notification

// todo #note when I added if(savedInstanceState != null) {..} and onRestore lines, AND in the app click BACK button to check the right count number, nothing happens
// todo change package name
// todo decide about minimum sdk
// todo #important #refactor: compare with code from tuition
public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    TextView mGoodPostureTextView;
    TextView mBadPostureTextView;
    private static final String PREF_KEY_GOOD_POSTURE_COUNT = "good_posture_count";
    private static final String PREF_KEY_BAD_POSTURE_COUNT = "bad_posture_count";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoodPostureTextView = findViewById(R.id.txt_good_posture_count);
        mBadPostureTextView = findViewById(R.id.txt_bad_posture_count);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.d(LOG_TAG, "A: created");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE);
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        TextView notifOffInfo = findViewById(R.id.notif_off_warning);
        String sharedPrefsFile = BuildConfig.APPLICATION_ID;
        SharedPreferences preferences = getSharedPreferences(sharedPrefsFile, MODE_PRIVATE);
        boolean notificationsOn = preferences.getBoolean("pref_key_switch_notifications", false); //defValue must be the same in preferences.xml
        if (!notificationsOn) {
            notifOffInfo.setVisibility(View.VISIBLE);
        } else {
            notifOffInfo.setVisibility(View.GONE);
        }
        setCountersPrefs();

        Log.d(LOG_TAG, "A: started");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "A: stopped");
    }


    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "A: destroying");
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setCountersPrefs();
    }

    private void setCountersPrefs() {
        SharedPreferences preferences = getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE);
        mGoodPostureTextView.setText(String.format("%d", preferences.getInt(PREF_KEY_GOOD_POSTURE_COUNT, 0)));
        mBadPostureTextView.setText(String.format("%d", preferences.getInt(PREF_KEY_BAD_POSTURE_COUNT, 0)));
    }
}
