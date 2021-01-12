package com.jakubokrasa.uprightchallenge.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.dr1009.app.chronodialogpreference.ChronoPreferenceFragment
import com.dr1009.app.chronodialogpreference.TimeDialogPreference
import com.jakubokrasa.uprightchallenge.BuildConfig
import com.jakubokrasa.uprightchallenge.R
import com.jakubokrasa.uprightchallenge.RepeatingNotifHelper
import com.jakubokrasa.uprightchallenge.service.RepeatingNotifService

/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : ChronoPreferenceFragment() {
    private lateinit var preferences: SharedPreferences // TODO: 11/18/2020 Shared prefs handling to distinct file
    private lateinit var prefsEditor: Editor
    private lateinit var notifHelper: RepeatingNotifHelper
    private lateinit var mainViewModel: MainViewModel


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        val startServiceIntent = Intent(context, RepeatingNotifService::class.java)
        requireContext().startService(startServiceIntent)
        notifHelper = RepeatingNotifHelper(requireContext())
        setPreferencesFromResource(R.xml.preferences, rootKey)
        if ((activity as AppCompatActivity).getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE) != null) {
            preferences = (activity as AppCompatActivity).getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE)
        }
        enableOrDisablePrefsRelatedWithNotifs()
        val notificationSwitch = findPreference<SwitchPreferenceCompat>("pref_key_switch_notifications")
        val intervalListPref = findPreference<ListPreference>("pref_key_interval")
        val timeNotifBeginPref: TimeDialogPreference? = findPreference(requireContext().resources.getString(R.string.pref_key_notif_on_time))
        val timeNotifEndPref: TimeDialogPreference? = findPreference(requireContext().resources.getString(R.string.pref_key_notif_off_time))
        notificationSwitch?.let {
            it.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, notificationsOnObject ->
                Log.d(LOG_TAG, "notif switch preference change")
                val notificationsOn = notificationsOnObject as Boolean
                if (notificationsOn) {
                    Log.d(LOG_TAG, "notifications on")
                        notifHelper.setNotifAlarm()
                        notifHelper.setSaveStatsAlarm()
                        notifHelper.setNotifOnTimeRange()
                } else {
                    Log.d(LOG_TAG, "notifications off")
                    notifHelper.turnOffNotifications()
                    Toast.makeText(context, "The notifications will be turned back on at " +  // TODO: 1/2/2021 make it compatible with 12 hours mode (in prefs time is always in 24h mode
                            preferences.getString(this.resources.getString(R.string.pref_key_notif_on_time), "07:30"), Toast.LENGTH_LONG).show()
                }
                prefsEditor.putBoolean(preference.key, notificationsOn).apply()
                enableOrDisablePrefsRelatedWithNotifs()
                if (intervalListPref != null) intervalListPref.isEnabled = !preferences.getBoolean("pref_key_cb_test_interval", false)
                true
            }
        }
        timeNotifBeginPref?.let {
            it.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                val time = newValue as String
                prefsEditor.putString(requireContext().resources.getString(R.string.pref_key_notif_on_time), time).apply()
                notifHelper.setNotifOnTimeRange()
                true
            }
        }

        timeNotifEndPref?.let {
            it.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                val time = newValue as String
                prefsEditor.putString(requireContext().resources.getString(R.string.pref_key_notif_off_time), time).apply()
                notifHelper.setNotifOnTimeRange()
                true
            }
        }
        intervalListPref?.let {
            intervalListPref.isEnabled = !preferences.getBoolean("pref_key_cb_test_interval", false)
            Log.d(LOG_TAG, "intervalListPref: $intervalListPref")
            it.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                val interval = newValue as String
                prefsEditor.putLong(preference.key, interval.toLong()).apply()
                notifHelper.setNotifAlarm()
                true
            }
        }

        if(BuildConfig.DEBUG) {
            val populateWithSampleDataBtnPref = findPreference<Preference>("pref_key_populate")
            populateWithSampleDataBtnPref?.let {
                it.isVisible = true
                it.onPreferenceClickListener = Preference.OnPreferenceClickListener { //Although it's not a good practice to call Database layer method from UI,
                    //it was done only for app testing/debugging purposes
                    mainViewModel.populateDbWithSampleData()
                    true
                }
            }
            val testIntervalCbPref = findPreference<Preference>(requireContext().resources.getString(R.string.pref_key_cb_test_interval))
            testIntervalCbPref?.let {
                it.isVisible = true
//                intervalListPref.isEnabled = false
                it.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                    val isTestInterval = newValue as Boolean
                    prefsEditor.putBoolean("pref_key_cb_test_interval", isTestInterval).apply()
                    if(intervalListPref!=null) intervalListPref.isEnabled = !isTestInterval
                    notifHelper.setNotifAlarm()
                    true
                }
            }
        }

    }

    override fun onStart() {
        super.onStart()
        prefsEditor = preferences.edit()
        val notificationSwitch = findPreference<SwitchPreferenceCompat>("pref_key_switch_notifications")
        if (notificationSwitch != null) {
            notificationSwitch.isChecked = preferences.getBoolean("pref_key_switch_notifications", false)
        }
    }

    private fun enableOrDisablePrefsRelatedWithNotifs() {
        val intervalListPref = findPreference<ListPreference>("pref_key_interval")
        val timeNotifBeginPref = findPreference<TimeDialogPreference>(resources.getString(R.string.pref_key_notif_on_time))
        val timeNotifEndPref = findPreference<TimeDialogPreference>(resources.getString(R.string.pref_key_notif_off_time))
        val testIntervalPref = findPreference<CheckBoxPreference>(requireContext().resources.getString(R.string.pref_key_cb_test_interval))
        val notifOn = preferences.getBoolean("pref_key_switch_notifications", false)
        intervalListPref!!.isEnabled = notifOn
        timeNotifBeginPref!!.isEnabled = notifOn
        timeNotifEndPref!!.isEnabled = notifOn
        testIntervalPref!!.isEnabled = notifOn
    }

    companion object {
        const val sharedPrefsFile = BuildConfig.APPLICATION_ID
        const val NOTIFICATION_ID = 0
        const val RESET_ALARM_ID = 1
        const val NOTIF_ON_TIME_ALARM = 2
        const val NOTIF_OFF_TIME_ALARM = 3
        private val LOG_TAG = SettingsFragment::class.java.simpleName
    }
}