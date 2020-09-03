package com.example.uprightchallenge.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Timestamp;

// TODO: 9/3/2020 add stat timestamp field
@Entity
public class PostureStat {

    @PrimaryKey (autoGenerate = true)
    @NonNull
    private long statId;


    private int positiveCount;

    private int negativeCount;

    public PostureStat(long statId, int positiveCount, int negativeCount) {
        this.statId = statId;
        this.positiveCount = positiveCount;
        this.negativeCount = negativeCount;
    }

    public long getStatId() { return statId; }

    public int getPositiveCount() {
        return positiveCount;
    }

    public int getNegativeCount() {
        return negativeCount;
    }
}
