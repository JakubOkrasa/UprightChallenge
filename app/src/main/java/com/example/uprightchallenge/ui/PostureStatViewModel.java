package com.example.uprightchallenge.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.uprightchallenge.data.PostureStat;
import com.example.uprightchallenge.data.PostureStatRepository;

import java.util.List;

public class PostureStatViewModel extends AndroidViewModel {
    private PostureStatRepository mRepository;
    private List<PostureStat> mAllStats;

    public PostureStatViewModel(@NonNull Application application) {
        super(application);
        this.mRepository = new PostureStatRepository(application);
        mAllStats = mRepository.getAllStats();
        mRepository.insert(new PostureStat(77, 4, 5));
        mRepository.logAllStats();
    }

    public void refreshAllStats() { mAllStats = mRepository.getAllStats(); }

    public List<PostureStat> getAllStats() {return mAllStats;}

    public void insert(PostureStat postureStat) { mRepository.insert(postureStat);}
}
