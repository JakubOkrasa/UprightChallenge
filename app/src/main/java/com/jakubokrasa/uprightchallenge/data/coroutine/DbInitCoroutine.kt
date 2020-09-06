package com.jakubokrasa.uprightchallenge.data.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

abstract class DbInitCoroutine {
    abstract fun populateWithSampleData()

    fun execute() {
        CoroutineScope(IO).launch {
            populateWithSampleData()
        }
    }
}