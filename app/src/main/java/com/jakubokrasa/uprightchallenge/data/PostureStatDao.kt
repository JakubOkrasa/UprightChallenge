package com.jakubokrasa.uprightchallenge.data

import androidx.room.Dao
import androidx.room.Query

@Dao
interface PostureStatDao {
    @androidx.room.Insert
    fun insert(postureStat: PostureStat?)

    @get:Query("SELECT * from PostureStat ORDER BY statId ASC")
    val allStats: List<PostureStat?>?

    @androidx.room.Query("DELETE FROM PostureStat")
    fun deleteAll()
}