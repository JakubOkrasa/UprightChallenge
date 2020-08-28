package com.example.uprightchallenge.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.uprightchallenge.data.PostureStat;
import com.example.uprightchallenge.data.PostureStatRepository;

import java.util.List;

public class PostureStatViewModel extends AndroidViewModel {
    private PostureStatRepository mRepository;
    private LiveData<List<PostureStat>> mAllStats;

    public PostureStatViewModel(@NonNull Application application) {
        super(application);
        this.mRepository = new PostureStatRepository(application);
        mAllStats = mRepository.getAllStats();
        insert(new PostureStat(7, 7));
    }

    public LiveData<List<PostureStat>> getAllStats() {return mAllStats;}

    public void insert(PostureStat postureStat) { mRepository.insert(postureStat);}
}
