package com.example.uprightchallenge.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

abstract class DbInitCoroutine {
    abstract fun populateWithTestData()

    fun execute() {
        CoroutineScope(IO).launch {
            populateWithTestData()
        }
    }
}