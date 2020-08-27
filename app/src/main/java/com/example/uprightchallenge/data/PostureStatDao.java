package com.example.uprightchallenge.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;


@Dao
public interface PostureStatDao {
    @Insert
    void insert(PostureStat postureStat);

    @Query("SELECT * from PostureStat ORDER BY statId ASC")
    void getAllStats();

    @Query("DELETE FROM PostureStat")
    void deleteAll();


}
