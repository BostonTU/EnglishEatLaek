package com.example.englisheatleak;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * DataManager สำหรับจัดการข้อมูล SharedPreferences ของเกม English Eat Laek
 * ควบคุม High Score, การตั้งค่าเปิด/ปิดเสียง และการบันทึก/กู้คืนสถานะเกม
 */
public class DataManager {

    private static final String PREFS_NAME = "EnglishEatLaekPrefs";
    
    // Preference Keys
    private static final String KEY_HIGH_SCORE = "high_score";
    private static final String KEY_SOUND_ENABLED = "sound_enabled";
    
    // Saved Game State Keys
    private static final String KEY_HAS_SAVED_GAME = "has_saved_game";
    private static final String KEY_SAVED_SCORE = "saved_score";
    private static final String KEY_SAVED_STREAK = "saved_streak";
    private static final String KEY_SAVED_FAT_LEVEL = "saved_fat_level";
    private static final String KEY_SAVED_WORD_ID = "saved_word_id";
    private static final String KEY_SAVED_TIME_MS = "saved_time_ms";

    private static DataManager instance;
    private final SharedPreferences prefs;

    public DataManager(Context context) {
        this(context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE));
    }

    public DataManager(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context);
        }
        return instance;
    }

    // ==========================================
    // High Score Management
    // ==========================================

    public int getHighScore() {
        return prefs.getInt(KEY_HIGH_SCORE, 0);
    }

    public boolean saveHighScore(int score) {
        int currentHigh = getHighScore();
        if (score > currentHigh) {
            prefs.edit().putInt(KEY_HIGH_SCORE, score).apply();
            return true;
        }
        return false;
    }

    public void resetHighScore() {
        prefs.edit().remove(KEY_HIGH_SCORE).apply();
    }

    // ==========================================
    // Settings Management
    // ==========================================

    public boolean isSoundEnabled() {
        return prefs.getBoolean(KEY_SOUND_ENABLED, true);
    }

    public void setSoundEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply();
    }

    // ==========================================
    // Game State Recovery & Persistence
    // ==========================================

    public void saveGameState(int score, int streak, int fatLevel, int wordId, long remainingTimeMs) {
        prefs.edit()
                .putBoolean(KEY_HAS_SAVED_GAME, true)
                .putInt(KEY_SAVED_SCORE, score)
                .putInt(KEY_SAVED_STREAK, streak)
                .putInt(KEY_SAVED_FAT_LEVEL, fatLevel)
                .putInt(KEY_SAVED_WORD_ID, wordId)
                .putLong(KEY_SAVED_TIME_MS, remainingTimeMs)
                .apply();
    }

    public boolean hasSavedGameState() {
        return prefs.getBoolean(KEY_HAS_SAVED_GAME, false);
    }

    public int getSavedScore() {
        return prefs.getInt(KEY_SAVED_SCORE, 0);
    }

    public int getSavedStreak() {
        return prefs.getInt(KEY_SAVED_STREAK, 0);
    }

    public int getSavedFatLevel() {
        return prefs.getInt(KEY_SAVED_FAT_LEVEL, 0);
    }

    public int getSavedWordId() {
        return prefs.getInt(KEY_SAVED_WORD_ID, -1);
    }

    public long getSavedTimeRemainingMs() {
        return prefs.getLong(KEY_SAVED_TIME_MS, 10000L);
    }

    public void clearSavedGameState() {
        prefs.edit()
                .remove(KEY_HAS_SAVED_GAME)
                .remove(KEY_SAVED_SCORE)
                .remove(KEY_SAVED_STREAK)
                .remove(KEY_SAVED_FAT_LEVEL)
                .remove(KEY_SAVED_WORD_ID)
                .remove(KEY_SAVED_TIME_MS)
                .apply();
    }
}
