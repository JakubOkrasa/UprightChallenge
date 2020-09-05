package com.example.uprightchallenge.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// TODO: 9/3/2020 add stat timestamp field
// TODO: 9/4/2020 explain in comment what is PostureStat
@Entity
public class PostureStat {

    @PrimaryKey (autoGenerate = true)
    @NonNull
    private long statId;


    private int correctPostureCount;

    private int badPostureCount;

    public PostureStat(long statId, int correctPostureCount, int badPostureCount) {
        this.statId = statId;
        this.correctPostureCount = correctPostureCount;
        this.badPostureCount = badPostureCount;
    }

    public long getStatId() { return statId; }

    public int getCorrectPostureCount() {
        return correctPostureCount;
    }

    public int getBadPostureCount() {
        return badPostureCount;
    }
}
