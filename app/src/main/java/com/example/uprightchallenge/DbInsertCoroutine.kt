package com.example.uprightchallenge

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class DbInsertCoroutine<Params, Progress, Result> {
    abstract fun doInBackground(params: Params): Result

    fun execute(vararg params: Params) {
        GlobalScope.launch(Dispatchers.Default) { // TODO: 8/28/2020 check coroutineScope
            doInBackground(params[0])
        }
    }
}