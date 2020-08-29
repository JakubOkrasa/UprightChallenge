package com.example.uprightchallenge

import kotlinx.coroutines.*

abstract class DbSelectCoroutine<Params, Progress, Result> {
    abstract fun doInBackground(): Result

    abstract fun onPostExecute(result: Result?)

    fun execute(): Result {
        GlobalScope.launch(Dispatchers.Default) { // TODO: 8/28/2020 check coroutineScope
            val result = doInBackground()
            withContext(Dispatchers.Main) {
                onPostExecute(result)
            }

        }
//        runBlocking {
//            launch {//background thread
//                val result = doInBackground()
//                withContext(Dispatchers.Main) {
//                    onPostExecute(result)
//                }
//            }
//        }
        /// ... // main thread
    }
}