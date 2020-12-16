package com.jakubokrasa.uprightchallenge.data

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import java.lang.StringBuilder

class PostureStatRepository(private val psDao: PostureStatDao) {
    suspend fun insertPostureStat(ps: PostureStat) {
        psDao.insertPostureStat(ps)
    }

    fun getAllStats(): LiveData<List<PostureStat>> {
        return psDao.getAllStats()
    }

    suspend fun deleteAllStats() {
        psDao.deleteAllStats()
    }

    companion object {
        private var INSTANCE: PostureStatRepository? = null

        fun getRepository(psDao: PostureStatDao): PostureStatRepository? {
            if (INSTANCE != null) { return INSTANCE }
            synchronized(PostureStatRepository::class.java) { // todo synchronized is deprecated
                INSTANCE = PostureStatRepository(psDao)
                return INSTANCE
            }
        }
    }
}