package com.jakubokrasa.uprightchallenge.data.coroutine

import com.jakubokrasa.uprightchallenge.data.PostureStat
import com.jakubokrasa.uprightchallenge.data.PostureStatDao
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO

abstract class DbInsertCoroutine {

    open fun insertStat(dao: PostureStatDao, stat: PostureStat) {
        CoroutineScope(IO).launch  {
            dao.insert(stat)
        }
    }

}
