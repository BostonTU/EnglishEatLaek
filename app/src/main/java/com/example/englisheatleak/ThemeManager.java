
package com.example.englisheatleak;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {

    public static final String SYSTEM = "system";
    public static final String LIGHT = "light";
    public static final String DARK = "dark";

    private static final String PREF_NAME = "theme_settings";
    private static final String KEY_THEME = "selected_theme";

    public static String getTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                PREF_NAME, Context.MODE_PRIVATE
        );

        return prefs.getString(KEY_THEME, SYSTEM);
    }

    public static void setTheme(Context context, String theme) {
        SharedPreferences prefs = context.getSharedPreferences(
                PREF_NAME, Context.MODE_PRIVATE
        );

        prefs.edit().putString(KEY_THEME, theme).apply();

        applyTheme(theme);
    }

    public static void applySavedTheme(Context context) {
        applyTheme(getTheme(context));
    }

    private static void applyTheme(String theme) {
        switch (theme) {
            case LIGHT:
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO
                );
                break;

            case DARK:
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_YES
                );
                break;

            default:
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                );
                break;
        }
    }
}
