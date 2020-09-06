package com.jakubokrasa.uprightchallenge.data;

import android.app.Application;
import android.content.Context;

import com.jakubokrasa.uprightchallenge.data.coroutine.DbInsertCoroutine;
import com.jakubokrasa.uprightchallenge.data.coroutine.DbSelectCoroutine;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class PostureStatRepository {
    private PostureStatDatabase database;
    private PostureStatDao mDao;
    private List<PostureStat> mAllStats;
    private static PostureStatRepository INSTANCE;


    private PostureStatRepository(Application application) {
        database = PostureStatDatabase.getDatabase(application);
        this.mDao = database.getPostureStatDao();
        this.mAllStats = getAllStats();
    }

    public static PostureStatRepository getRepository(final Context context) {
        if(INSTANCE == null) {
            synchronized (PostureStatDatabase.class) {
                if(INSTANCE==null) {
                    //it might not work because ApplicationContext is not always the same what Application
                    //if this method will be called at first by PostureViewModel, everything should work fine
                    INSTANCE = new PostureStatRepository((Application)context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
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

    public void insert(PostureStat stat) { new insertCoroutine().execute(mDao, stat); }

    //only for debug
    public String statsToString(List<PostureStat> stats) {
        StringBuilder sb = new StringBuilder();
        sb.append("\nPosture stats:\n\tid\t\t+\t\t-\n");
        for (int i = 0; i < stats.size(); i++) {
            PostureStat ps = stats.get(i);
            sb.append(String.format("\t%d\t\t%d\t\t%d\n", ps.getStatId(), ps.getCorrectPostureCount(), ps.getBadPostureCount()));
        }
        return sb.toString();
    }

    private static class insertCoroutine extends DbInsertCoroutine {
        @Override
        public void execute(@NotNull PostureStatDao dao, @NotNull PostureStat stat) {
            super.execute(dao, stat);
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
