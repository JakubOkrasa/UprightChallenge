package com.example.uprightchallenge.data.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

abstract class DbInitCoroutine {
    abstract fun populateWithTestData()

    fun execute() {
        CoroutineScope(IO).launch {
            populateWithTestData()
        }
    }
}