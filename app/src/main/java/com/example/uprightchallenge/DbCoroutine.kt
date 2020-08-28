package com.example.uprightchallenge

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class DbCoroutine<Params, Progress, Result> {
    abstract fun doInBackground(): Result

    fun execute() {
        GlobalScope.launch(Dispatchers.Default) {
            doInBackground()
        }
    }
}