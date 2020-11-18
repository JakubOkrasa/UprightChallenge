package com.jakubokrasa.uprightchallenge.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.dr1009.app.chronodialogpreference.ChronoPreferenceFragment
import com.dr1009.app.chronodialogpreference.TimeDialogPreference
import com.jakubokrasa.uprightchallenge.BuildConfig
import com.jakubokrasa.uprightchallenge.R
import com.jakubokrasa.uprightchallenge.RepeatingNotifHelper
import com.jakubokrasa.uprightchallenge.data.PostureStatDatabase
import com.jakubokrasa.uprightchallenge.service.RepeatingNotifService

/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : ChronoPreferenceFragment() {
    private lateinit var preferences: SharedPreferences // TODO: 11/18/2020 Shared prefs handling to distinct file
    private lateinit var prefsEditor: Editor
    private lateinit var notifHelper: RepeatingNotifHelper

    override fun onCreatePreferences(savedInstanceState: Bundle, rootKey: String) {
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
        if (notificationSwitch != null) {
            notificationSwitch.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, notificationsOnObject ->
                Log.d(LOG_TAG, "notif switch preference change")
                val notificationsOn = notificationsOnObject as Boolean
                if (notificationsOn) {
                    Log.d(LOG_TAG, "notifications on")
                        notifHelper.setAlarmPendingIntent()
                        notifHelper.setResetPendingIntent()
                        notifHelper.setNotifOnTimeRange()
                } else {
                    Log.d(LOG_TAG, "notifications off")
                    notifHelper!!.turnOffNotifications()
                }
                // save changes to SharedPreferences
                prefsEditor!!.putBoolean(preference.key, notificationsOn).apply()
                enableOrDisablePrefsRelatedWithNotifs()
                true
            }
        }
        timeNotifBeginPref.let {
            timeNotifBeginPref!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                val time = newValue as String
                prefsEditor.putString(requireContext().resources.getString(R.string.pref_key_notif_on_time), time).apply()
                notifHelper.setNotifOnTimeRange()
                true
            }
        }

        timeNotifEndPref.let {
            timeNotifEndPref!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                val time = newValue as String
                prefsEditor.putString(requireContext().resources.getString(R.string.pref_key_notif_off_time), time).apply()
                notifHelper.setNotifOnTimeRange()
                true
            }
        }
        intervalListPref.let {
            Log.d(LOG_TAG, "intervalListPref: $intervalListPref")
            intervalListPref!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                val interval = newValue as String
                prefsEditor.putLong(preference.key, interval.toLong()).apply()
                notifHelper.setAlarmPendingIntent()
                true
            }
        }


        //only for tests
        val populateWithSampleDataBtnPref = findPreference<Preference>("pref_key_populate")
        if (populateWithSampleDataBtnPref != null) {
            populateWithSampleDataBtnPref.onPreferenceClickListener = Preference.OnPreferenceClickListener { //Although it's not a good practice to call Database layer method from UI,
                //it was done only for app testing/debugging purposes and will be removed in the official
                // version of the app TODO remove in official version
                PostureStatDatabase.getDatabase(requireContext()).populateDbWithSampleData()
                true
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
        intervalListPref!!.isEnabled = preferences.getBoolean("pref_key_switch_notifications", false)
        timeNotifBeginPref!!.isEnabled = preferences.getBoolean("pref_key_switch_notifications", false)
        timeNotifEndPref!!.isEnabled = preferences.getBoolean("pref_key_switch_notifications", false)
    }

    companion object {
        const val SCHEDULED_NOTIF_OFF_ACTION = BuildConfig.APPLICATION_ID + ".SCHEDULED_NOTIF_OFF_ACTION"
        const val SCHEDULED_NOTIF_ON_ACTION = BuildConfig.APPLICATION_ID + ".SCHEDULED_NOTIF_ON_ACTION"
        const val sharedPrefsFile = BuildConfig.APPLICATION_ID
        const val NOTIFICATION_ID = 0
        const val RESET_ALARM_ID = 1
        const val NOTIF_ON_TIME_ALARM = 2
        private val LOG_TAG = SettingsFragment::class.java.simpleName
    }
}