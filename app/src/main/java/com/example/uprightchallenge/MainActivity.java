package com.example.uprightchallenge;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
// todo #later show number of daily count in notification
// todo #bug notifications aren't turn on directly after installing the app. User have to turn off and on notifications switch at the beginning

// todo #note when I added if(savedInstanceState != null) {..} and onRestore lines, AND in the app click BACK button to check the right count number, nothing happens
// todo change package name
// todo decide about minimum sdk
public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private CorrectPostureReceiver mCorrectPostureReceiver = new CorrectPostureReceiver(this);
    static final String POSTURE_YES_ACTION = BuildConfig.APPLICATION_ID + ".POSTURE_YES_ACTION";
    private SharedPreferences preferences;
    TextView mCorrectPostureTextView;
    private static final String KEY_YES_POSTURE_COUNT = "yes_posture_count";

    //@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String sharedPrefFile = BuildConfig.APPLICATION_ID;
        preferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

        //ToggleButton mNotifToggle = findViewById(R.id.notify_toggle);
        mCorrectPostureTextView = findViewById(R.id.txt_count);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        registerReceiver(mCorrectPostureReceiver, new IntentFilter(POSTURE_YES_ACTION));
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
        boolean notificationsOn = preferences.getBoolean("switch_notifications", false); //defValue must be the same in preferences.xml
        if(!notificationsOn) {
            notifOffInfo.setVisibility(View.VISIBLE);
        }
        else {
            notifOffInfo.setVisibility(View.GONE);
        }
        mCorrectPostureTextView.setText(String.format("%d", preferences.getInt(KEY_YES_POSTURE_COUNT, 0)));

        Log.d(LOG_TAG, "A: started");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "A: stopped");
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(mCorrectPostureReceiver);
        Log.d(LOG_TAG, "A: destroying");
        super.onDestroy();
    }




}
