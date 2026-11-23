package com.example.englisheatleak;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;

/**
 * SoundManager สำหรับจัดการเสียงเอฟเฟกต์ในเกม
 * ใช้ ToneGenerator ในการสังเคราะห์เสียงเอฟเฟกต์ ตอบถูก ตอบผิด และกดปุ่ม
 * ตรวจสอบการตั้งค่าเปิด/ปิดเสียงจาก DataManager ก่อนเล่นเสียงเสมอ
 */
public class SoundManager {

    private static SoundManager instance;
    private final DataManager dataManager;
    private ToneGenerator toneGenerator;

    public SoundManager(Context context) {
        this.dataManager = DataManager.getInstance(context);
        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 80);
        } catch (Exception e) {
            toneGenerator = null;
        }
    }

    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context);
        }
        return instance;
    }

    public void playCorrectSound() {
        if (!dataManager.isSoundEnabled() || toneGenerator == null) return;
        try {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150);
        } catch (Exception ignored) {
        }
    }

    public void playWrongSound() {
        if (!dataManager.isSoundEnabled() || toneGenerator == null) return;
        try {
            toneGenerator.startTone(ToneGenerator.TONE_SUP_ERROR, 250);
        } catch (Exception ignored) {
        }
    }

    public void playButtonClickSound() {
        if (!dataManager.isSoundEnabled() || toneGenerator == null) return;
        try {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 40);
        } catch (Exception ignored) {
        }
    }

    public void release() {
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
    }
}
