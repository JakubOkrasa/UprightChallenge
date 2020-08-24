package com.example.uprightchallenge;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
// todo #later show number of daily count in notification

// todo #note when I added if(savedInstanceState != null) {..} and onRestore lines, AND in the app click BACK button to check the right count number, nothing happens
// todo change package name
// todo decide about minimum sdk
public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private GoodPostureReceiver mGoodPostureReceiver = new GoodPostureReceiver(this);
    private BadPostureReceiver mBadPostureReceiver = new BadPostureReceiver(this);
    static final String GOOD_POSTURE_ACTION = BuildConfig.APPLICATION_ID + ".GOOD_POSTURE_ACTION";
    static final String BAD_POSTURE_ACTION = BuildConfig.APPLICATION_ID + ".BAD_POSTURE_ACTION";
    TextView mGoodPostureTextView;
    TextView mBadPostureTextView;
    private static final String PREF_KEY_GOOD_POSTURE_COUNT = "good_posture_count";
    private static final String PREF_KEY_BAD_POSTURE_COUNT = "bad_posture_count";

    //@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ToggleButton mNotifToggle = findViewById(R.id.notify_toggle);
        mGoodPostureTextView = findViewById(R.id.txt_good_posture_count);
        mBadPostureTextView = findViewById(R.id.txt_bad_posture_count);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        registerReceiver(mGoodPostureReceiver, new IntentFilter(GOOD_POSTURE_ACTION));
        registerReceiver(mBadPostureReceiver, new IntentFilter(BAD_POSTURE_ACTION));
        //todo dlaczego nie ma rejestracji mAlarmManager?

        Log.d(LOG_TAG, "A: created");
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
        if(!notificationsOn) {
            notifOffInfo.setVisibility(View.VISIBLE);
        }
        else {
            notifOffInfo.setVisibility(View.GONE);
        }
        mGoodPostureTextView.setText(String.format("%d", preferences.getInt(PREF_KEY_GOOD_POSTURE_COUNT, 0)));
        mBadPostureTextView.setText(String.format("%d", preferences.getInt(PREF_KEY_BAD_POSTURE_COUNT, 0)));

        Log.d(LOG_TAG, "A: started");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "A: stopped");
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(mGoodPostureReceiver);
        unregisterReceiver(mBadPostureReceiver);
        Log.d(LOG_TAG, "A: destroying");
        super.onDestroy();
    }
}
