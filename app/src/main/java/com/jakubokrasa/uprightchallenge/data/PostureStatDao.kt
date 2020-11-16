package com.jakubokrasa.uprightchallenge.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PostureStatDao {
    @Insert
    suspend fun insert(postureStat: PostureStat?)

    @Query("SELECT * from PostureStat ORDER BY statId ASC")
    suspend fun getAllStats(): List<PostureStat?>?

    @Query("DELETE FROM PostureStat")
    suspend fun deleteAll()
}