package com.example.projectmanagement.ui.statistics;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.projectmanagement.data.model.Statistics;
import com.example.projectmanagement.data.service.StatisticsService;

public class StatisticsViewModel extends AndroidViewModel {
    private static final String TAG = "StatisticsViewModel";
    private final MutableLiveData<Statistics> statisticsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public StatisticsViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Statistics> getStatistics() {
        return statisticsLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadStatistics() {
        isLoading.setValue(true);
        error.setValue(null);

        StatisticsService.getStatistics(
                getApplication(),
                statistics -> {
                    statisticsLiveData.setValue(statistics);
                    isLoading.setValue(false);
                },
                volleyError -> {
                    Log.e(TAG, "Error loading statistics", volleyError);
                    error.setValue(volleyError.getMessage());
                    isLoading.setValue(false);
                }
        );
    }
}