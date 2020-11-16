package com.jakubokrasa.uprightchallenge.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jakubokrasa.uprightchallenge.data.coroutine.DbInitCoroutine

@Database(entities = [PostureStat::class], version = 1, exportSchema = false)
abstract class PostureStatDatabase : RoomDatabase() {
    abstract val postureStatDao: PostureStatDao

    fun populateDbWithSampleData() {
        PopulateDbCoroutine(INSTANCE).execute()
    }

    private class PopulateDbCoroutine(db: PostureStatDatabase?) : DbInitCoroutine() {
        private val mDao: PostureStatDao
        var positiveCount = intArrayOf(1, 2, 1, 2, 4, 5, 6, 6, 7, 5, 8, 6, 7, 10)
        var negativeCount = intArrayOf(5, 5, 4, 3, 5, 4, 2, 3, 2, 4, 3, 3, 2, 1)
        override fun populateWithSampleData() {
            mDao.deleteAll()
            for (i in positiveCount.indices) {
                val ps = PostureStat(0.toLong(), positiveCount[i], negativeCount[i])
                mDao.insert(ps)
            }
            Log.d(LOG_TAG, "DB populated with sample data.")
        }

        companion object {
            val LOG_TAG: String = PopulateDbCoroutine::class.java.getSimpleName()
        }

        init {
            mDao = db!!.postureStatDao
        }
    }

    companion object {
        private var INSTANCE: PostureStatDatabase? = null
        fun getDatabase(context: Context): PostureStatDatabase? {
            if (INSTANCE == null) {
                synchronized(PostureStatDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context.applicationContext,
                                PostureStatDatabase::class.java, "PostureStat")
                                .fallbackToDestructiveMigration()
                                .addCallback(sRoomDatabaseCallback)
                                .build()
                    }
                }
            }
            return INSTANCE
        }

        private val sRoomDatabaseCallback: Callback = object : Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
            }
        }
    }
}