package com.jakubokrasa.uprightchallenge.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.jakubokrasa.uprightchallenge.BuildConfig
import com.jakubokrasa.uprightchallenge.data.PostureStat
import com.jakubokrasa.uprightchallenge.data.PostureStatDao
import com.jakubokrasa.uprightchallenge.data.PostureStatDatabase
import com.jakubokrasa.uprightchallenge.data.PostureStatRepository
import kotlinx.coroutines.runBlocking

class ResetAlarmReceiver() : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val preferences = context.getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE)
        val prefsEditor = preferences.edit()
        val psDao : PostureStatDao = PostureStatDatabase
                .getDatabase(context)
                .postureStatDao()

        // save stats to repository
        val mRepository: PostureStatRepository = PostureStatRepository.getRepository(psDao)!!
        runBlocking {
            mRepository.insertPostureStat(PostureStat(
                    0,
                    preferences.getInt(PREF_KEY_GOOD_POSTURE_COUNT, 0),
                    preferences.getInt(PREF_KEY_BAD_POSTURE_COUNT, 0)))
        }

        // reset posture counters
        prefsEditor.putInt(PREF_KEY_BAD_POSTURE_COUNT, 0)
        prefsEditor.putInt(PREF_KEY_GOOD_POSTURE_COUNT, 0)
        prefsEditor.apply()
        Log.d(LOG_TAG, "new PostureStat saved")
    }

    companion object {
        const val sharedPrefsFile = BuildConfig.APPLICATION_ID
        private const val PREF_KEY_GOOD_POSTURE_COUNT = "good_posture_count"
        private const val PREF_KEY_BAD_POSTURE_COUNT = "bad_posture_count"
        val LOG_TAG = ResetAlarmReceiver::class.java.simpleName
    }
}