package com.example.uprightchallenge.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.uprightchallenge.DbInsertCoroutine;

import java.util.List;

public class PostureStatRepository {
    private PostureStatDao mDao;
    private LiveData<List<PostureStat>> mAllStats;

    public PostureStatRepository(Application application) {
        PostureStatDatabase db = PostureStatDatabase.getDatabase(application);
        this.mDao = db.getPostureStatDao();
        this.mAllStats = mDao.getAllStats();
    }

    public LiveData<List<PostureStat>> getAllStats() {return mAllStats; }

    public void insert(PostureStat stat) { new insertCoroutine(mDao).execute(stat); }

    private static class insertCoroutine extends DbInsertCoroutine<PostureStat, Void, Void> {
        private PostureStatDao coroutineDao;
        insertCoroutine(PostureStatDao dao) {
            coroutineDao = dao;
        }

        @Override
        public Void doInBackground(PostureStat postureStat) { //todo not sure. In tutorial here was (Word... words)
                                                                //todo but I don't no how to do respective method in kotlin coroutine
                                                                //todo maybe it was required by AsyncTask
            coroutineDao.insert(postureStat);
            return null;
        }

    }
}
