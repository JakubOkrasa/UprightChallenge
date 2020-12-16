package com.jakubokrasa.uprightchallenge.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PostureStatDao {
    @Insert
    suspend fun insertPostureStat(postureStat: PostureStat?)

    @Query("SELECT * from PostureStat ORDER BY statId ASC")
    fun getAllStats(): LiveData<List<PostureStat>>

    @Query("DELETE FROM PostureStat")
    suspend fun deleteAllStats()
}