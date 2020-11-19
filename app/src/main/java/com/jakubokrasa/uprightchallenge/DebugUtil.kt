package com.jakubokrasa.uprightchallenge

import com.jakubokrasa.uprightchallenge.data.PostureStat
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

//only for debug
fun logAllStats(stats: List<PostureStat>): String {
    return statsToString(stats)
}

//only for debug
fun statsToString(stats: List<PostureStat>): String {
    val sb = StringBuilder()
    sb.append("\nPosture stats:\n\tid\t\t+\t\t-\n")
    for (stat in stats) {
        val (statId, correctPostureCount, badPostureCount) = stat
        sb.append("\t$statId\t\t$correctPostureCount\t\t$badPostureCount\n")
    }
    return sb.toString()
}

//for debugging only
fun getTime(): String {
    return SimpleDateFormat("HH:mm:ss", Locale.ROOT).format(Calendar.getInstance().time)
}