package com.example.uprightchallenge.data;

import android.app.Application;

import com.example.uprightchallenge.coroutine.DbInsertCoroutine;
import com.example.uprightchallenge.coroutine.DbSelectCoroutine;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class PostureStatRepository {
    private PostureStatDao mDao;
    private List<PostureStat> mAllStats;

    public PostureStatRepository(Application application) {
        PostureStatDatabase db = PostureStatDatabase.getDatabase(application);
        this.mDao = db.getPostureStatDao();
        this.mAllStats = getAllStats();
    }

    public List<PostureStat> getAllStats() {
        try {
            return  new getAllStatsCoroutine().execute(mDao).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void insert(PostureStat stat) { new insertCoroutine().insertStat(mDao, stat); }

    //only for debug
    public String statsToString(List<PostureStat> stats) {
        StringBuilder sb = new StringBuilder();
        sb.append("\nPosture stats:\n\tid\t\t+\t\t-\n");
        for (int i = 0; i < stats.size(); i++) {
            PostureStat ps = stats.get(i);
            sb.append(String.format("\t%d\t\t%d\t\t%d\n", ps.getStatId(), ps.getPositiveCount(), ps.getNegativeCount()));
        }
        return sb.toString();
    }

    private static class insertCoroutine extends DbInsertCoroutine {
        @Override
        public void insertStat(@NotNull PostureStatDao dao, @NotNull PostureStat stat) {
            super.insertStat(dao, stat);
        }
    }


    private static class getAllStatsCoroutine extends DbSelectCoroutine {
        @NotNull
        @Override
        public CompletableFuture<List<PostureStat>> execute(@NotNull PostureStatDao dao) {
            return super.execute(dao);
        }
    }
}
