package com.jakubokrasa.uprightchallenge.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
The name of the class PostureStat stands for posture statistics. Throughout the code there are variables
which names include "postureStat" or just "stat". Although this word means something else in English, the
word was used in the app's code to represent user's result from a single day.
 */
@Entity
data class PostureStat(
        @field:PrimaryKey(autoGenerate = true) val statId: Long,
        val correctPostureCount: Int,
        val badPostureCount: Int
)