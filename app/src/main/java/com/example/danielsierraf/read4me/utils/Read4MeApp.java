package com.example.danielsierraf.read4me.utils;

import android.app.Application;
import android.content.Context;

/**
 * Created by danielsierraf on 8/23/15.
 */
public class Read4MeApp extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this.getApplicationContext();
    }

    public static Context getInstance() { return context; }
}
