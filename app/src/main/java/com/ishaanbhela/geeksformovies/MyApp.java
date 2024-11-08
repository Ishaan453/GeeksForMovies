package com.ishaanbhela.geeksformovies;
import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

public class MyApp extends Application {
    public static final String url = "https://6kq47z2tx3hp3v2ppoxqtazyhu0urefh.lambda-url.ap-south-1.on.aws/";

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }
}
