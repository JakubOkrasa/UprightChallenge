package com.jakubokrasa.uprightchallenge.ui

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.jakubokrasa.uprightchallenge.BuildConfig
import com.jakubokrasa.uprightchallenge.R
import com.jakubokrasa.uprightchallenge.data.PostureStat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.text.NumberFormat
import java.util.*

// todo #later show daily progress in notifications
// todo extract getting preferences method
class MainActivity : AppCompatActivity(), OnSharedPreferenceChangeListener {
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        mainViewModel.getAllStats().observe(this, {
            stats -> showStatsChart(stats)
        })
        Log.d(LOG_TAG, "A: onCreate")
    }

    override fun onResume() {
        super.onResume()
        val preferences = getSharedPreferences(SettingsFragment.sharedPrefsFile, MODE_PRIVATE)
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.itemId
        if (id == R.id.action_settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        val sharedPrefsFile = BuildConfig.APPLICATION_ID
        val preferences = getSharedPreferences(sharedPrefsFile, MODE_PRIVATE)
        val notificationsOn = preferences.getBoolean("pref_key_switch_notifications", false) //defValue must be the same in preferences.xml
        if (!notificationsOn) {
            notif_off_warning.visibility = View.VISIBLE
        } else {
            notif_off_warning.visibility = View.GONE
        }
        txt_percent_stat.text = getPercentStat()
//        showStatsChart() //todo if it is not done by observer, show stats onStart
        Log.d(LOG_TAG, "A: onStart")
    }

    override fun onStop() {
        super.onStop()
        Log.d(LOG_TAG, "A: onStop")
    }

    override fun onDestroy() {
        Log.d(LOG_TAG, "A: onDestroy")
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == PREF_KEY_BAD_POSTURE_COUNT || key == PREF_KEY_GOOD_POSTURE_COUNT) {
            txt_percent_stat.text = getPercentStat()
        }
    }


    private fun getPercentStat(): String {
        val preferences = getSharedPreferences(SettingsFragment.sharedPrefsFile, MODE_PRIVATE)
        val correctPostureCount = preferences.getInt(PREF_KEY_GOOD_POSTURE_COUNT, 0)
        val badPostureCount = preferences.getInt(PREF_KEY_BAD_POSTURE_COUNT, 0)
        return if (correctPostureCount == 0 && badPostureCount == 0) { "--" }
        else {
            val result = correctPostureCount.toFloat() / (correctPostureCount + badPostureCount).toFloat()
            setPercentStatColor(result)
            val percentageFormat = NumberFormat.getPercentInstance(Locale.US)
            return percentageFormat.format(result.toFloat()) }
    }

    private fun setPercentStatColor(result: Float) {
        val color: Int =
            if (result > 0 && result < 0.4) {
                R.color.red
            } else if (result >= 0.4 && result < 0.6) {
                R.color.orange
            } else if (result >= 0.6 && result < 0.8) {
                R.color.green
            } else if (result >= 0.8 && result <= 1) {
                R.color.MediumSeaGreen
            } else {
                R.color.colorPrimaryDark
            }
        txt_percent_stat.setTextColor(ContextCompat.getColor(this, color))
    }

    private fun showStatsChart(postureStats: List<PostureStat>) {
        val percentBars = getPercentBarEntries(postureStats)
        if (percentBars.isNotEmpty()) {
            val usageBars = getUsageBarEntries(postureStats)
            adjustChartStyle(stats_chart)
            val barData = BarData(getPercentDataSet(percentBars), getUsageDataSet(usageBars))
            barData.barWidth = 0.45f
            stats_chart.data = barData
            setClosingChartProperties(stats_chart, barData)
        }
    }

    private fun getPercentDataSet(percentBars: List<BarEntry>): BarDataSet {
        val percentDataSet = BarDataSet(percentBars, resources.getString(R.string.chart_percentStat_label))
        percentDataSet.color = ContextCompat.getColor(this, R.color.green)
        percentDataSet.valueFormatter = PercentFormatter()
        percentDataSet.valueTextSize = 14f
        return percentDataSet
    }

    private fun getUsageDataSet(usageBars: List<BarEntry>): BarDataSet {
        val usageDataSet = BarDataSet(usageBars, resources.getString(R.string.chart_usageStat_label))
        usageDataSet.color = ContextCompat.getColor(this, R.color.light_blue)
        usageDataSet.valueFormatter = IntegerValueFormatter()
        usageDataSet.valueTextSize = 14f
        return usageDataSet
    }

    private fun adjustChartStyle(chart: BarChart) {
        chart.animateY(800)
        chart.axisLeft.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.xAxis.isEnabled = false
        chart.legend.textSize = 12f
        chart.legend.isWordWrapEnabled = true
        chart.setFitBars(true)
        chart.extraBottomOffset = 4f
        chart.description.isEnabled = false
    }

    private fun getPercentBarEntries(postureStats: List<PostureStat>): List<BarEntry> {
            val entries: MutableList<BarEntry> = ArrayList()
            for ((index, stat) in postureStats.withIndex()) {
                entries.add(BarEntry(index.toFloat(), getPercentageOfCorrectPostures(stat)))
            }
            return entries
    }
    private fun getUsageBarEntries(postureStats: List<PostureStat>): List<BarEntry> {
            val entries: MutableList<BarEntry> = ArrayList()
            for ((index, stat) in postureStats.withIndex()) {
                entries.add(BarEntry(index.toFloat(), getSumOfCorrectAndBadPostures(stat)))
            }
            return entries
}
    //these properties have to be set after calling setData(BarData) method
    private fun setClosingChartProperties(chart: BarChart, barData: BarData) {
        chart.xAxis.axisMaximum = barData.xMax + 1f
        chart.xAxis.axisMinimum = -0.5f
        chart.setVisibleXRangeMaximum(5f)
        chart.groupBars(0f, 0.06f, 0.02f)
        // (0.02 + 0.45) * 2 + 0.06 = 1.00 -> interval per "group"
        // 0.45 - bar width
    }

    private fun getPercentageOfCorrectPostures(stat: PostureStat): Float {
        val correctPostureCount = stat.correctPostureCount
        val badPostureCount = stat.badPostureCount
        return if (correctPostureCount == 0 && badPostureCount == 0) { 0f }
        else { 100 * correctPostureCount.toFloat() / (badPostureCount.toFloat() + correctPostureCount.toFloat()) }
    }

    private fun getSumOfCorrectAndBadPostures(stat: PostureStat): Float {
        return (stat.badPostureCount + stat.correctPostureCount).toFloat()
    }

    companion object {
        private val LOG_TAG = MainActivity::class.java.simpleName
        private const val PREF_KEY_GOOD_POSTURE_COUNT = "good_posture_count"
        private const val PREF_KEY_BAD_POSTURE_COUNT = "bad_posture_count"
    }
}