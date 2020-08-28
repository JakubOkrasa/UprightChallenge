package com.example.uprightchallenge.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.uprightchallenge.DbCoroutine;

public abstract class PostureStatDatabase extends RoomDatabase {
    private static PostureStatDatabase INSTANCE;

    public static PostureStatDatabase getDatabase(final Context context) {
        if(INSTANCE == null) {
            synchronized (PostureStatDatabase.class) {
                if(INSTANCE==null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            PostureStatDatabase.class, "PostureStat")
                            .fallbackToDestructiveMigration()
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract PostureStatDao getPostureStatDao();

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new PopulateDbCoroutine(INSTANCE).execute();
        }
    };

    private static class PopulateDbCoroutine extends DbCoroutine<Void, Void, Void> {
        private final PostureStatDao mDao;
        int[] positiveCount = {10, 15, 8};
        int[] negativeCount = {3, 2, 1};

        public PopulateDbCoroutine(PostureStatDatabase db) {
            this.mDao = db.getPostureStatDao();
        }

        @Override
        public Void doInBackground() {
            for (int i = 0; i < positiveCount.length; i++) {
                PostureStat ps = new PostureStat(positiveCount[i], negativeCount[i]);
                mDao.insert(ps);
            }
            return null;
        }


    }

}
