package com.jakubokrasa.uprightchallenge.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.jakubokrasa.uprightchallenge.data.PostureStat
import com.jakubokrasa.uprightchallenge.data.PostureStatDatabase
import com.jakubokrasa.uprightchallenge.data.PostureStatRepository
import kotlinx.coroutines.launch

class PostureStatViewModel(application: Application) : AndroidViewModel(application) {
    private val mRepository: PostureStatRepository

    init {
        val psDao = PostureStatDatabase
                .getDatabase(application)
                .postureStatDao()
        mRepository = PostureStatRepository(psDao)
    }

    fun getAllStats(): LiveData<List<PostureStat>> {
        return mRepository.getAllStats()
    }

    fun insertPostureStat(ps: PostureStat) = viewModelScope.launch {
        mRepository.insertPostureStat(ps)
    }

    fun deleteAllStats() = viewModelScope.launch {
        mRepository.deleteAllStats()
    }

    fun populateDbWithSampleData() {
        val positiveCounts = intArrayOf(1, 2, 1, 2, 4, 5, 6, 6, 7, 5, 8, 6, 7, 10)
        val negativeCounts = intArrayOf(5, 5, 4, 3, 5, 4, 2, 3, 2, 4, 3, 3, 2, 1)
        deleteAllStats()
        for (i in positiveCounts) {
            val ps = PostureStat(0.toLong(), positiveCounts[i], negativeCounts[i])
            insertPostureStat(ps)
        }
    }
}