package com.example.uprightchallenge.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/*
The name of the class PostureStat stands for posture statistics. Throughout the code there are variables
which names include "postureStat" or just "stat". Although this word means something else in English, the
word was used in the app's code to represent user's result from a single day.
 */

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
