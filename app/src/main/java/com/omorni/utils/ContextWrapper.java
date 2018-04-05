package com.omorni.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

/**
 * Created by AIM on 3/21/2018.
 */

public class ContextWrapper extends android.content.ContextWrapper {

    public ContextWrapper(Context base) {
        super(base);
    }

    public static ContextWrapper wrap(Context context, String language) {

        Resources res = context.getResources();
        Configuration configuration = res.getConfiguration();
        Locale locale;

        if (language.equalsIgnoreCase("en")) {
            locale = new Locale("en", "MA");
        } else {
            locale = new Locale("ar", "MA");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Locale.setDefault(locale);
            configuration.setLocales(new LocaleList(locale));
            configuration.setLayoutDirection(locale);
            persist(context, language);
            res.updateConfiguration(configuration, res.getDisplayMetrics());
        } else {
            configuration.locale = locale;
            configuration.setLayoutDirection(locale);
            persist(context, language);
            res.updateConfiguration(configuration, res.getDisplayMetrics());
        }


        return new ContextWrapper(context);
    }

    private static void persist(Context context, String language) {
        SavePref.setString(context, "app_language", language);
    }

}

