package com.jakubokrasa.uprightchallenge.data

import androidx.lifecycle.LiveData
import java.lang.StringBuilder

class PostureStatRepository(private val psDao: PostureStatDao) {
//    private val database: PostureStatDatabase
//    private val mDao: PostureStatDao
//    private val mAllStats: List<PostureStat>?
//
//    init {
//        database = PostureStatDatabase.getDatabase(application)
//        mDao = database.getPostureStatDao()
//        mAllStats = allStats
//    }


    suspend fun insertPostureStat(ps: PostureStat) {
        psDao.insertPostureStat(ps)
    }

    fun getAllStats(): LiveData<List<PostureStat>> {
        return psDao.getAllStats()
    }

    suspend fun deleteAllStats() {
        psDao.deleteAllStats()
    }


//    }

    // TODO: 11/17/2020 shouldn't be the repo a singleton?
//    companion object {
//        private var INSTANCE: PostureStatRepository? = null
//        fun getRepository(context: Context): PostureStatRepository? {
//            if (INSTANCE == null) {
//                synchronized(PostureStatDatabase::class.java) {
//                        //it might not work because ApplicationContext is not always the same what Application
//                        //if this method will be called at first by PostureViewModel, everything should work fine
//                        INSTANCE = PostureStatRepository(context.applicationContext as Application)
//                }
//            }
//            return INSTANCE
//        }

    // TODO: 11/17/2020 move to PostureStatViewModel

}