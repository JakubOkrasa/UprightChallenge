package com.example.uprightchallenge.data.coroutine

import com.example.uprightchallenge.data.PostureStat
import com.example.uprightchallenge.data.PostureStatDao
import kotlinx.coroutines.*
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture

abstract class DbSelectCoroutine {
    private suspend fun getAllStats(dao: PostureStatDao): List<PostureStat> =
        coroutineScope {
            dao.allStats
    }

    open fun execute(dao: PostureStatDao): CompletableFuture<List<PostureStat>> =
            GlobalScope.future { getAllStats(dao) }

}