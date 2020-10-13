package com.jakubokrasa.uprightchallenge.ui;

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
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.jakubokrasa.uprightchallenge.BuildConfig;
import com.jakubokrasa.uprightchallenge.R;
import com.jakubokrasa.uprightchallenge.data.PostureStat;

import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.jakubokrasa.uprightchallenge.ui.SettingsFragment.NOTIF_ON_TIME_ACTION;
import static com.jakubokrasa.uprightchallenge.ui.SettingsFragment.NOTIF_OFF_TIME_ACTION;
import static com.jakubokrasa.uprightchallenge.ui.SettingsFragment.sharedPrefsFile;

// todo #later show daily progress in notifications
// todo extract getting preferences method
public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private PostureStatViewModel mPostureStatVM;

    TextView mPercentStatTextView;
    private static final String PREF_KEY_GOOD_POSTURE_COUNT = "good_posture_count";
    private static final String PREF_KEY_BAD_POSTURE_COUNT = "bad_posture_count";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPercentStatTextView = findViewById(R.id.txt_percent_stat);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPostureStatVM = new ViewModelProvider(this).get(PostureStatViewModel.class);

        Log.d(LOG_TAG, "A: onCreate");
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
        mPercentStatTextView.setText(getPercentStat());
        showStatsChart();

        Log.d(LOG_TAG, "A: onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "A: onStop");
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "A: onDestroy");
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PREF_KEY_BAD_POSTURE_COUNT) || key.equals(PREF_KEY_GOOD_POSTURE_COUNT)) {
            mPercentStatTextView.setText(getPercentStat());
        }
    }

    private String getPercentStat() {
        SharedPreferences preferences = getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE);
        int correctPostureCount = preferences.getInt(PREF_KEY_GOOD_POSTURE_COUNT, 0);
        int badPostureCount = preferences.getInt(PREF_KEY_BAD_POSTURE_COUNT, 0);
        if(correctPostureCount==0 && badPostureCount == 0) { return "--"; }
        else {
            float result = (float)correctPostureCount / (float)(correctPostureCount + badPostureCount);
            setPercentStatColor(result);
            NumberFormat percentageFormat = NumberFormat.getPercentInstance(Locale.US);
            return percentageFormat.format(result);
        }
    }

    private void setPercentStatColor(float result) {
        int color;
        if(result>0 && result < 0.4) { color = R.color.red; }
        else if(result>=0.4 && result < 0.6) { color = R.color.orange; }
        else if(result>=0.6 && result < 0.8) { color = R.color.green; }
        else if(result>=0.8 && result <= 1) { color = R.color.MediumSeaGreen; }
        else { color = R.color.colorPrimaryDark; }
        mPercentStatTextView.setTextColor(ContextCompat.getColor(this, color));
    }

    private void showStatsChart() {
        List<BarEntry> percentBars = getPercentBarEntries();
        if (percentBars.size() > 0) {
            List<BarEntry> usageBars = getUsageBarEntries();
            BarChart chart = findViewById(R.id.stats_chart);
            adjustChartStyle(chart);
            BarData barData = new BarData(getPercentDataSet(percentBars), getUsageDataSet(usageBars));
            barData.setBarWidth(0.45f);
            chart.setData(barData);
            setClosingChartProperties(chart, barData);
        }

    }

    @NotNull
    private BarDataSet getPercentDataSet(List<BarEntry> percentBars) {
        BarDataSet percentDataSet = new BarDataSet(percentBars, getResources().getString(R.string.chart_percentStat_label));
        percentDataSet.setColor(ContextCompat.getColor(this, R.color.green));
        percentDataSet.setValueFormatter(new PercentFormatter());
        percentDataSet.setValueTextSize(14f);
        return percentDataSet;
    }

    @NotNull
    private BarDataSet getUsageDataSet(List<BarEntry> usageBars) {
        BarDataSet usageDataSet = new BarDataSet(usageBars, getResources().getString(R.string.chart_usageStat_label));
        usageDataSet.setColor(ContextCompat.getColor(this, R.color.light_blue));
        usageDataSet.setValueFormatter(new IntegerValueFormatter());
        usageDataSet.setValueTextSize(14f);
        return usageDataSet;
    }

    private void adjustChartStyle(BarChart chart) {
        chart.animateY(800);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setEnabled(false);
        chart.getLegend().setTextSize(12f);
        chart.getLegend().setWordWrapEnabled(true);
        chart.setFitBars(true);
        chart.setExtraBottomOffset(4f);
        chart.getDescription().setEnabled(false);
    }

    private List<BarEntry> getPercentBarEntries() {
        List<BarEntry> entries = new ArrayList<>();
        List<PostureStat> stats = mPostureStatVM.getAllStats();
        for (int i = 0; i < stats.size(); i++) {
            entries.add(new BarEntry(i, getPercentageOfCorrectPostures(stats.get(i))));
        }
        return entries;
    }

    private List<BarEntry> getUsageBarEntries() {
        List<BarEntry> entries = new ArrayList<>();
        List<PostureStat> stats = mPostureStatVM.getAllStats();
        for (int i = 0; i < stats.size(); i++) {
            entries.add(new BarEntry(i, getSumOfCorrectAndBadPostures(stats.get(i))));
        }
        return entries;
    }

    //these properties have to be set after calling setData(BarData) method
    private void setClosingChartProperties(BarChart chart, BarData barData) {
        chart.getXAxis().setAxisMaximum(barData.getXMax() + 1f);
        chart.getXAxis().setAxisMinimum(-0.5f);
        chart.setVisibleXRangeMaximum(5);
        chart.groupBars(0f, 0.06f, 0.02f);
        // (0.02 + 0.45) * 2 + 0.06 = 1.00 -> interval per "group"
        // 0.45 - bar width
    }

    private float getPercentageOfCorrectPostures(PostureStat stat) {
        int correctPostureCount = stat.getCorrectPostureCount();
        int badPostureCount = stat.getBadPostureCount();
        if(correctPostureCount==0 && badPostureCount == 0) { return 0; }
        else { return 100 * (float)correctPostureCount / ((float)badPostureCount + (float)correctPostureCount); }
    }

    private float getSumOfCorrectAndBadPostures(PostureStat stat) {
        return stat.getBadPostureCount() + stat.getCorrectPostureCount();
    }

    public void nightON(View view) {
        sendBroadcast(new Intent(NOTIF_OFF_TIME_ACTION));
    }

    public void nightOFF(View view) {
        sendBroadcast(new Intent(NOTIF_ON_TIME_ACTION));
    }

    public void checkNotifSw(View view) {
        SharedPreferences preferences = getSharedPreferences(sharedPrefsFile, MODE_PRIVATE);
        TextView tv = (TextView) view;
        if (preferences.getBoolean("pref_key_switch_notifications", true)) {
            tv.setText("notif are ON");
        } else {
            tv.setText("notif are OFF");
        }
    }
}
