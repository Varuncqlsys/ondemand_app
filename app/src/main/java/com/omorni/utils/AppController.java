package com.omorni.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;


/**
 * Created by cqlsys on 1/15/2016.
 */
public class AppController extends Application {

    public static final String TAG = AppController.class.getSimpleName();

    private static AppController mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        SavePref savePref = new SavePref();
//        LocaleHelper.setLocale(getApplicationContext(), savePref.getString(getApplicationContext(), "app_language", "ar"));
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        SavePref savePref = new SavePref();
//        LocaleHelper.setLocale(getApplicationContext(), savePref.getString(getApplicationContext(), "app_language", "ar"));
    }



    @Override
    public void onTerminate() {
        super.onTerminate();
    }




}