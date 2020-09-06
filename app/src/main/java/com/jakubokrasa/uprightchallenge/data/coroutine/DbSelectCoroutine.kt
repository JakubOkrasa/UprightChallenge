package com.jakubokrasa.uprightchallenge.data.coroutine

import com.jakubokrasa.uprightchallenge.data.PostureStat
import com.jakubokrasa.uprightchallenge.data.PostureStatDao
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