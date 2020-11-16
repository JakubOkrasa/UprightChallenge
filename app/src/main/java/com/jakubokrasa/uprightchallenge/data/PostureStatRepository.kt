package com.jakubokrasa.uprightchallenge.data

import android.app.Application
import android.content.Context
import com.jakubokrasa.uprightchallenge.data.coroutine.DbInsertCoroutine
import com.jakubokrasa.uprightchallenge.data.coroutine.DbSelectCoroutine
import java.lang.InterruptedException
import java.lang.StringBuilder
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class PostureStatRepository private constructor(application: Application) {
    private val database: PostureStatDatabase
    private val mDao: PostureStatDao
    private val mAllStats: List<PostureStat>?
    val allStats: List<PostureStat>?
        get() {
            try {
                return getAllStatsCoroutine().execute(mDao).get()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            return null
        }

    fun insert(stat: PostureStat?) {
        insertCoroutine().execute(mDao, stat)
    }

    //only for debug
    fun statsToString(stats: List<PostureStat>): String {
        val sb = StringBuilder()
        sb.append("\nPosture stats:\n\tid\t\t+\t\t-\n")
        for (i in stats.indices) {
            val (statId, correctPostureCount, badPostureCount) = stats[i]
            sb.append(String.format("\t%d\t\t%d\t\t%d\n", statId, correctPostureCount, badPostureCount))
        }
        return sb.toString()
    }

    private class insertCoroutine : DbInsertCoroutine() {
        fun execute(@NotNull dao: PostureStatDao?, @NotNull stat: PostureStat?) {
            super.execute(dao, stat!!)
        }
    }

    private class getAllStatsCoroutine : DbSelectCoroutine() {
        @NotNull
        override fun execute(@NotNull dao: PostureStatDao?): CompletableFuture<List<PostureStat>> {
            return super.execute(dao)
        }
    }

    companion object {
        private var INSTANCE: PostureStatRepository? = null
        fun getRepository(context: Context): PostureStatRepository? {
            if (INSTANCE == null) {
                synchronized(PostureStatDatabase::class.java) {
                    if (INSTANCE == null) {
                        //it might not work because ApplicationContext is not always the same what Application
                        //if this method will be called at first by PostureViewModel, everything should work fine
                        INSTANCE = PostureStatRepository(context.getApplicationContext() as Application)
                    }
                }
            }
            return INSTANCE
        }
    }

    init {
        database = PostureStatDatabase.getDatabase(application)
        mDao = database.getPostureStatDao()
        mAllStats = allStats
    }
}