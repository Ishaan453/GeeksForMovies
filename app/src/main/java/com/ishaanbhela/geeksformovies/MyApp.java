package com.ishaanbhela.geeksformovies;
import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

public class MyApp extends Application {
    public static final String url = "https://2f2vjaxr6x6uqpdvqmwpmwch6a0pzera.lambda-url.ap-south-1.on.aws/";

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }
}
