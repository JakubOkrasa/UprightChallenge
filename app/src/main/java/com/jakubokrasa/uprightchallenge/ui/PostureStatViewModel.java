package com.jakubokrasa.uprightchallenge.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.jakubokrasa.uprightchallenge.data.PostureStat;
import com.jakubokrasa.uprightchallenge.data.PostureStatRepository;

import java.util.List;

public class PostureStatViewModel extends AndroidViewModel {
    private PostureStatRepository mRepository;
    private List<PostureStat> mAllStats;

    public PostureStatViewModel(@NonNull Application application) {
        super(application);
        this.mRepository = PostureStatRepository.getRepository(application);
        mAllStats = mRepository.getAllStats();
    }

    public void refreshAllStats() { mAllStats = mRepository.getAllStats(); }

    public List<PostureStat> getAllStats() {return mAllStats;}

    public void insert(PostureStat postureStat) { mRepository.insert(postureStat);}

    public String logAllStats(List<PostureStat> stats) { return mRepository.statsToString(stats);}
}
