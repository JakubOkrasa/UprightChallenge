package com.example.uprightchallenge.data;

import android.app.Application;
import android.util.Log;

import com.example.uprightchallenge.DbInsertCoroutine;
import com.example.uprightchallenge.DbSelectCoroutine;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PostureStatRepository {
    private PostureStatDao mDao;
    private List<PostureStat> mAllStats;

    public PostureStatRepository(Application application) {
        PostureStatDatabase db = PostureStatDatabase.getDatabase(application);
        this.mDao = db.getPostureStatDao();
//        this.mAllStats = mDao.getAllStats();
    }

//    public List<PostureStat> getAllStats() {return mAllStats; }

    public void insert(PostureStat stat) { new insertCoroutine(mDao).execute(stat); }

    public void logAllStats() { new logAllStatsCoroutine(mDao).execute();}

    public List<PostureStat> getAllStats() {return  new getAllStatsCoroutine(mDao).execute(); }

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

    private static class logAllStatsCoroutine extends DbSelectCoroutine<Void, Void, List<PostureStat>> {
        private PostureStatDao coroutineDao;
        public logAllStatsCoroutine(PostureStatDao coroutineDao) {
            this.coroutineDao = coroutineDao;
        }

        @Override
        public List<PostureStat> doInBackground() {
            return coroutineDao.getAllStats();
        }

        @Override
        public void onPostExecute(@Nullable List<PostureStat> postureStats) {
            for (int i = 0; i < postureStats.size(); i++) {
                PostureStat ps = postureStats.get(i);
                Log.e(PostureStatRepository.class.getSimpleName(), String.format("entry: id: %d +: %d -: %d", ps.getStatId(), ps.getPositiveCount(), ps.getNegativeCount()));
            }
        }
    }

    private static class getAllStatsCoroutine extends DbSelectCoroutine<Void, Void, List<PostureStat>> {
        private PostureStatDao coroutineDao;
        public getAllStatsCoroutine(PostureStatDao coroutineDao) {
            this.coroutineDao = coroutineDao;
        }

        @Override
        public List<PostureStat> doInBackground() {
            return coroutineDao.getAllStats();
        }

        @Override
        public void onPostExecute(@Nullable List<PostureStat> postureStats) {
            //do nothing
        }
    }
}
