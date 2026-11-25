package com.example.englisheatleak;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

/** Keeps the app UI language independent from the vocabulary direction. */
public final class LanguageManager {
    public static final String THAI = "th";
    public static final String ENGLISH = "en";

    private static final String PREFS = "language_settings";
    private static final String KEY_LANGUAGE = "language";

    private LanguageManager() {}

    public static String getLanguage(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_LANGUAGE, THAI);
    }

    public static void setLanguage(Context context, String language) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY_LANGUAGE, language).apply();
    }

    public static Context wrap(Context context) {
        String language = getLanguage(context);
        Locale locale = Locale.forLanguageTag(language);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration);
    }
}
