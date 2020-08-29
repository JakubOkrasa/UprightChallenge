package com.example.uprightchallenge

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// TODO: 8/29/2020 change globalScope to coroutine scope
abstract class DbInitCoroutine<Params, Progress, Result> {
    abstract fun doInBackground(): Result

    fun execute() {
        GlobalScope.launch(Dispatchers.Default) {
            doInBackground()
        }
    }
}