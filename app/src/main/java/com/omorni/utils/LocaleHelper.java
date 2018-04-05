package com.omorni.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.preference.PreferenceManager;
import android.support.v7.widget.ViewUtils;
import android.util.Log;

import java.util.Locale;

/**
 * Created by user on 5/12/2017.
 */

public class LocaleHelper {

    public static Context onAttach(Context context) {
        String lang = getPersistedData(context, Locale.getDefault().getLanguage());
        return setLocale(context, lang);
    }

    public static Context onAttach(Context context, String defaultLanguage) {
        String lang = getPersistedData(context, defaultLanguage);
        return setLocale(context, lang);
    }

    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    public static Context setLocale(Context context, String language) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources1(context, language);
        }

        return updateResourcesLegacy(context, language);
    }

    private static String getPersistedData(Context context, String defaultLanguage) {
        SavePref savePref = new SavePref();
        String app_language = savePref.getString(context, "app_language", defaultLanguage);
        return app_language;
    }

    private static void persist(Context context, String language) {
        SavePref.setString(context, "app_language", language);
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources2(Context context, String language) {

        Locale locale;
        if (language.equalsIgnoreCase("ar")) {
            locale = new Locale("ar");
        } else {
            locale = new Locale("en");
        }
        Configuration config = context.getResources().getConfiguration();
        Locale.setDefault(locale);
        config.setLocale(locale);
        config.setLayoutDirection(locale);
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());

        return context;

    }


    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources1(Context context, String language) {
        Locale locale;
        if (language.equalsIgnoreCase("en")) {
            locale = new Locale("en","MA");
        } else {
            locale = new Locale("ar","MA");
        }

        Resources resources = context.getResources();
        Configuration configuration = context.getResources().getConfiguration();
        Locale.setDefault(locale);
        configuration.setLocales(new LocaleList(locale));
        configuration.setLayoutDirection(locale);
        persist(context, language);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());


//        configuration.locale = locale;
//        configuration.setLayoutDirection(locale);
//        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
//        persist(context, language);

        return context;

    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context context, String language) {

        Locale locale;
        if (language.equalsIgnoreCase("ar")) {
            locale = new Locale("ar");
        } else {
            locale = new Locale("en");
        }
        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);

        LocaleList localeList = new LocaleList(locale);
        LocaleList.setDefault(localeList);
        configuration.setLocales(localeList);
        configuration.setLayoutDirection(locale);
        context = context.createConfigurationContext(configuration);
        return context;
    }

    @SuppressWarnings("deprecation")
    private static Context updateResourcesLegacy(Context context, String language) {
        SavePref savePref = new SavePref(context);
        Locale locale;

        if (language.equalsIgnoreCase("en")) {
            locale = new Locale("en","MA");
        } else {
            locale = new Locale("ar","MA");
        }
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        configuration.setLayoutDirection(locale);
        persist(context, language);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        return context;
    }
}
