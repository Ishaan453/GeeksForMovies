package com.ishaanbhela.geeksformovies;

import static android.content.ContentValues.TAG;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

public class MyApp extends Application {
    private String authKey;

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);




    }
}
