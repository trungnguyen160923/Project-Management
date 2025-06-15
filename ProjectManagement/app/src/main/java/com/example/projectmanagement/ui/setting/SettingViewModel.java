package com.example.projectmanagement.ui.setting;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SettingViewModel extends ViewModel {
    private static final String PREF_NAME = "theme_pref";
    private static final String KEY_DARK_MODE = "dark_mode";
    private final MutableLiveData<Boolean> isDarkMode = new MutableLiveData<>(false);
    private SharedPreferences preferences;

    public void init(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        isDarkMode.setValue(preferences.getBoolean(KEY_DARK_MODE, false));
    }

    public LiveData<Boolean> getDarkMode() {
        return isDarkMode;
    }

    public void setDarkMode(boolean isDark) {
        isDarkMode.setValue(isDark);
        if (preferences != null) {
            preferences.edit().putBoolean(KEY_DARK_MODE, isDark).apply();
        }
    }
}