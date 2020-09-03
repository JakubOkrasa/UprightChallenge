package com.example.uprightchallenge.data;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.uprightchallenge.coroutine.DbInitCoroutine;

@Database(entities = {PostureStat.class}, version = 1, exportSchema = false)
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
                    Log.e("Database", "db built");
                }
            }
        }
        return INSTANCE;
    }

    public abstract PostureStatDao getPostureStatDao();

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            Log.e("Database", "db opening");
            super.onOpen(db);
            new PopulateDbCoroutine(INSTANCE).execute();

        }
    };

    private static class PopulateDbCoroutine extends DbInitCoroutine {
        private final PostureStatDao mDao;
        int[] positiveCount = {10, 15, 8};
        int[] negativeCount = {3, 2, 1};

        public PopulateDbCoroutine(PostureStatDatabase db) {
            this.mDao = db.getPostureStatDao();
        }

        @Override
        public void populateWithTestData() {
            mDao.deleteAll();
            for (int i = 0; i < positiveCount.length; i++) {
                PostureStat ps = new PostureStat((long)i, positiveCount[i], negativeCount[i]);
                mDao.insert(ps);
            }
            Log.e("coroutine", "db populated");
        }


    }

}
