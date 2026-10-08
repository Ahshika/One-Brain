package com.onebrain.game.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/** User preferences, stored separately from game progress. */
public final class SettingsManager {

    public enum ThemeMode {
        SYSTEM,
        LIGHT,
        DARK
    }

    private static final String PREFS = "onebrain_settings";
    private static final String KEY_SOUND = "sound";
    private static final String KEY_HAPTICS = "haptics";
    private static final String KEY_TIMER = "show_timer";
    private static final String KEY_THEME = "theme";

    private final SharedPreferences prefs;

    public SettingsManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean isSoundEnabled() {
        return prefs.getBoolean(KEY_SOUND, true);
    }

    public void setSoundEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply();
    }

    public boolean isHapticsEnabled() {
        return prefs.getBoolean(KEY_HAPTICS, true);
    }

    public void setHapticsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_HAPTICS, enabled).apply();
    }

    public boolean isTimerVisible() {
        return prefs.getBoolean(KEY_TIMER, true);
    }

    public void setTimerVisible(boolean visible) {
        prefs.edit().putBoolean(KEY_TIMER, visible).apply();
    }

    public ThemeMode getThemeMode() {
        try {
            return ThemeMode.valueOf(prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name()));
        } catch (IllegalArgumentException e) {
            return ThemeMode.SYSTEM;
        }
    }

    public void setThemeMode(ThemeMode mode) {
        prefs.edit().putString(KEY_THEME, mode.name()).apply();
        applyTheme();
    }

    public void applyTheme() {
        int nightMode;
        switch (getThemeMode()) {
            case LIGHT:
                nightMode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case DARK:
                nightMode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            default:
                nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
        }
        if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
            AppCompatDelegate.setDefaultNightMode(nightMode);
        }
    }
}
