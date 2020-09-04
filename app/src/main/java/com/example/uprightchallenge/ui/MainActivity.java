package com.example.uprightchallenge.ui;

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
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.uprightchallenge.BuildConfig;
import com.example.uprightchallenge.R;
import com.example.uprightchallenge.data.PostureStat;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.example.uprightchallenge.ui.SettingsFragment.sharedPrefsFile;
// todo #later show number of daily count in notification

// todo change package name
// todo decide about minimum sdk
// todo #important #refactor: compare with code from tuition
// TODO: 9/3/2020 add ScrollView in content_main.xml
// TODO: 9/3/2020 add more options in repeat interval preference
public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private PostureStatViewModel mPostureStatVM;

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

        mPostureStatVM = new ViewModelProvider(this).get(PostureStatViewModel.class);

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
        mPostureStatVM.refreshAllStats();
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

        showStatsChart();

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

    private void showStatsChart() {
        List<BarEntry> posBars = getPosBars();
        if (posBars.size()>0) {
            BarChart chart = findViewById(R.id.stats_chart);
            BarDataSet set = new BarDataSet(posBars, "positive");
            chart.animateY(1500);
            BarData data = new BarData(set);
            data.setBarWidth(1f);
            set.setColor(R.color.colorPrimary);
            chart.setData(data);
        }

    }

    private ArrayList<BarEntry> getPosBars() {
        ArrayList<BarEntry> posBars = new ArrayList<>();
        List<PostureStat> stats = mPostureStatVM.getAllStats();
        for (int i = 0; i < stats.size(); i++) {
            posBars.add(new BarEntry(i, stats.get(i).getPositiveCount()));
        }
        return posBars;
    }

    // only for debug. This is on click test button event
    public void testSth(View view) {
        Log.d(LOG_TAG, mPostureStatVM.logAllStats(mPostureStatVM.getAllStats()));
    }
}
