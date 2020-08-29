package com.example.uprightchallenge.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Timestamp;

// TODO: 8/28/2020 refactor name PostureStat, stat means quickly
@Entity
public class PostureStat {

    @PrimaryKey
    @NonNull
    private long statId;

//    private Timestamp statTime;

    private int positiveCount;

    private int negativeCount;

    public PostureStat(long statId, int positiveCount, int negativeCount) {
        this.statId = statId;
//        this.statTime = statTime;
        this.positiveCount = positiveCount;
        this.negativeCount = negativeCount;
    }

//    public Timestamp getStatTime() {
//        return statTime;
//    }

    public int getPositiveCount() {
        return positiveCount;
    }

    public int getNegativeCount() {
        return negativeCount;
    }

    public long getStatId() {
        return statId;
    }
}
