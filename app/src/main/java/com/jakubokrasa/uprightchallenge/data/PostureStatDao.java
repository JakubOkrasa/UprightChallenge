package com.jakubokrasa.uprightchallenge.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;


@Dao
public interface PostureStatDao {
    @Insert
    void insert(PostureStat postureStat);

    @Query("SELECT * from PostureStat ORDER BY statId ASC")
    List<PostureStat> getAllStats();

    @Query("DELETE FROM PostureStat")
    void deleteAll();


}
