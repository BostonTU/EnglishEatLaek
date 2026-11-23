package com.example.englisheatleak;

import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class DataManagerTest {

    private DataManager dataManager;

    @Before
    public void setUp() {
        FakeSharedPreferences fakePrefs = new FakeSharedPreferences();
        dataManager = new DataManager(fakePrefs);
    }

    @Test
    public void testInitialHighScoreIsZero() {
        assertEquals(0, dataManager.getHighScore());
    }

    @Test
    public void testSaveHighScoreSuccess() {
        boolean updated = dataManager.saveHighScore(100);
        assertTrue(updated);
        assertEquals(100, dataManager.getHighScore());

        // Saving lower score should return false and keep 100
        boolean updatedLower = dataManager.saveHighScore(50);
        assertFalse(updatedLower);
        assertEquals(100, dataManager.getHighScore());

        // Saving higher score should update
        boolean updatedHigher = dataManager.saveHighScore(150);
        assertTrue(updatedHigher);
        assertEquals(150, dataManager.getHighScore());
    }

    @Test
    public void testResetHighScore() {
        dataManager.saveHighScore(200);
        assertEquals(200, dataManager.getHighScore());

        dataManager.resetHighScore();
        assertEquals(0, dataManager.getHighScore());
    }

    @Test
    public void testSoundSettingsToggle() {
        assertTrue(dataManager.isSoundEnabled());

        dataManager.setSoundEnabled(false);
        assertFalse(dataManager.isSoundEnabled());

        dataManager.setSoundEnabled(true);
        assertTrue(dataManager.isSoundEnabled());
    }

    @Test
    public void testSaveAndClearGameState() {
        assertFalse(dataManager.hasSavedGameState());

        dataManager.saveGameState(80, 4, 2, 10, 5000L);
        assertTrue(dataManager.hasSavedGameState());
        assertEquals(80, dataManager.getSavedScore());
        assertEquals(4, dataManager.getSavedStreak());
        assertEquals(2, dataManager.getSavedFatLevel());
        assertEquals(10, dataManager.getSavedWordId());
        assertEquals(5000L, dataManager.getSavedTimeRemainingMs());

        dataManager.clearSavedGameState();
        assertFalse(dataManager.hasSavedGameState());
    }

    // In-memory implementation of SharedPreferences for testing
    private static class FakeSharedPreferences implements SharedPreferences {
        private final Map<String, Object> map = new HashMap<>();

        @Override public Map<String, ?> getAll() { return map; }
        @Override public String getString(String key, String defValue) { Object v = map.get(key); return v instanceof String ? (String) v : defValue; }
        @Override public Set<String> getStringSet(String key, Set<String> defValues) { return defValues; }
        @Override public int getInt(String key, int defValue) { Object v = map.get(key); return v instanceof Integer ? (Integer) v : defValue; }
        @Override public long getLong(String key, long defValue) { Object v = map.get(key); return v instanceof Long ? (Long) v : defValue; }
        @Override public float getFloat(String key, float defValue) { Object v = map.get(key); return v instanceof Float ? (Float) v : defValue; }
        @Override public boolean getBoolean(String key, boolean defValue) { Object v = map.get(key); return v instanceof Boolean ? (Boolean) v : defValue; }
        @Override public boolean contains(String key) { return map.containsKey(key); }
        @Override public Editor edit() { return new FakeEditor(map); }
        @Override public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {}
        @Override public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {}

        private static class FakeEditor implements Editor {
            private final Map<String, Object> map;

            FakeEditor(Map<String, Object> map) { this.map = map; }

            @Override public Editor putString(String key, String value) { map.put(key, value); return this; }
            @Override public Editor putStringSet(String key, Set<String> values) { map.put(key, values); return this; }
            @Override public Editor putInt(String key, int value) { map.put(key, value); return this; }
            @Override public Editor putLong(String key, long value) { map.put(key, value); return this; }
            @Override public Editor putFloat(String key, float value) { map.put(key, value); return this; }
            @Override public Editor putBoolean(String key, boolean value) { map.put(key, value); return this; }
            @Override public Editor remove(String key) { map.remove(key); return this; }
            @Override public Editor clear() { map.clear(); return this; }
            @Override public boolean commit() { return true; }
            @Override public void apply() {}
        }
    }
}
