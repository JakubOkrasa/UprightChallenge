package com.example.uprightchallenge.data;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.uprightchallenge.data.coroutine.DbInitCoroutine;

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
                }
            }
        }
        return INSTANCE;
    }

    public abstract PostureStatDao getPostureStatDao();

    public void populateDbWithSampleData() {
        new PopulateDbCoroutine(INSTANCE).execute();
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);

        }
    };

    private static class PopulateDbCoroutine extends DbInitCoroutine {
        private final PostureStatDao mDao;
        int[] positiveCount = {1, 2, 1, 2, 4, 5, 6, 6, 7, 5, 8, 6, 7, 10};
        int[] negativeCount = {5, 5, 4, 3, 5, 4, 2, 3, 2, 4, 3, 3, 2, 1};
        public static final String LOG_TAG = PopulateDbCoroutine.class.getSimpleName();

        public PopulateDbCoroutine(PostureStatDatabase db) {
            this.mDao = db.getPostureStatDao();
        }

        @Override
        public void populateWithSampleData() {
            mDao.deleteAll();
            for (int i = 0; i < positiveCount.length; i++) {
                PostureStat ps = new PostureStat((long)0, positiveCount[i], negativeCount[i]);
                mDao.insert(ps);
            }
            Log.e(LOG_TAG, "DB populated with sample data.");
        }


    }

}
