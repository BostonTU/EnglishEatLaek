package com.example.englisheatleak;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.View;
import android.widget.ImageView;

/**
 * ควบคุม Animation ตัวละคร
 *
 * Sprite Sheet ทุกไฟล์ถูกจัดใหม่แล้ว: 7 เฟรม ขนาดเฟรมละเท่ากัน (240x224 px)
 * ตำแหน่งเท้า/กึ่งกลางตรงกันทุกเฟรมและทุกฉาก จึงแบ่งเป็น 7 ช่องเท่า ๆ กันได้ถูกต้อง
 * (ห้ามนำภาพต้นฉบับเดิมมาใช้กับโค้ดนี้)
 */
public class CharacterController {

    private static final int FRAME_COUNT = 7;

    // ขนาดตัวละครเพิ่มตามระดับความอ้วน (ใช้ร่วมกับภาพ Fat ไม่ได้ใช้แทน)
    private static final float SCALE_PER_LEVEL = 0.07f;
    private static final int MAX_SCALE_LEVEL = 7;          // สูงสุด 1.49 เท่า
    private static final int SCALE_ANIM_MS = 350;
    // ขนาดและตำแหน่งเท้าใน Canvas ของ Sprite (px)
    private static final float SHEET_W = 240f, SHEET_H = 224f, FEET_Y = 214f;

    // ความเร็ว (มิลลิวินาทีต่อเฟรม)
    private static final int IDLE_DELAY = 140;
    private static final int EATING_DELAY = 100;
    private static final int FAT_DELAY = 130;
    private static final int SKINNY_DELAY = 110;
    private static final int DEAD_DELAY = 130;
    // หายใจตอน Idle: ยืดตัวขึ้นลงเล็กน้อยโดยเท้าอยู่กับที่ (ตั้ง BREATH_AMOUNT = 0 เพื่อปิด)
    private static final float BREATH_AMOUNT = 0.02f;
    private static final int BREATH_MS = 900;          // ครึ่งรอบ (หายใจเข้าหรือออก)
    private static final int SHAKE_MS = 320;          // สั่น + กระพริบแดงตอนตอบผิด
    private static final int FALL_MS = 650;           // ล้มกระแทกพื้นตอนเริ่ม Dead
    private static final int DEFLATE_DELAY = 140;   // ยุบจากอ้วนทีละระดับก่อนผอม

    private final ImageView characterView;
    private final EffectPlayer effects;     // ข้อความ ประกาย ฝุ่น
    private final View shadowView;       // เงาใต้ตัวละคร (อาจเป็น null)
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final BitmapDrawable[] idleFrames;
    private final BitmapDrawable[][] eatingFrames; // index 0 = กินตอนตัวปกติ, 1-3 = กินตอนอ้วนระดับ 1-3
    private final BitmapDrawable[][] fatFrames;   // index 0 = อ้วนระดับ 1, 1 = ระดับ 2, ...
    private final BitmapDrawable[] skinnyFrames;
    private final BitmapDrawable[] deadFrames;

    private Runnable animationRunnable;
    private ValueAnimator breathing;
    private Runnable breathStarter;

    private int fatLevel = 0;          // จำนวนครั้งที่ตอบถูกสะสม (ไม่รีเซ็ตเมื่อเปลี่ยนคำถาม)
    private boolean isDead = false;
    private boolean isBusy = false;    // กำลังเล่น Animation กิน (ยังไม่รับคำตอบใหม่)

    /** แจ้ง UI ว่าตอนนี้รับคำตอบได้หรือไม่ (ใช้เปิด/ปิดปุ่ม) */
    public interface InputStateListener {
        void onInputEnabledChanged(boolean enabled);
    }

    private InputStateListener inputListener;

    public void setInputStateListener(InputStateListener listener) {
        this.inputListener = listener;
        notifyInput();
    }

    public boolean canAnswer() {
        return !isDead && !isBusy;
    }

    private void notifyInput() {
        if (inputListener != null) {
            inputListener.onInputEnabledChanged(canAnswer());
        }
    }

    public CharacterController(ImageView characterView) {
        this(characterView, null);
    }

    public CharacterController(ImageView characterView, View shadowView) {
        this.characterView = characterView;
        this.shadowView = shadowView;
        this.effects = new EffectPlayer(characterView);

        idleFrames = loadFrames(R.drawable.idle_7_frames);
        eatingFrames = new BitmapDrawable[][]{
                loadFrames(R.drawable.eating_7_frames),     // ตัวปกติ
                loadFrames(R.drawable.eating_f1_7_frames),  // กินตอนอ้วนระดับ 1
                loadFrames(R.drawable.eating_f2_7_frames),  // กินตอนอ้วนระดับ 2
                loadFrames(R.drawable.eating_f3_7_frames)   // กินตอนอ้วนระดับ 3+
        };
        fatFrames = new BitmapDrawable[][]{
                loadFrames(R.drawable.fat_7_frames),    // ระดับ 1: ภาพต้นฉบับ
                loadFrames(R.drawable.fat2_7_frames),   // ระดับ 2: สร้างจากภาพ fat
                loadFrames(R.drawable.fat3_7_frames)    // ระดับ 3: สร้างจากภาพ fat
        };
        skinnyFrames = loadFrames(R.drawable.skinny_7_frames);
        deadFrames = loadFrames(R.drawable.dead_7_frames);

        characterView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        characterView.setAdjustViewBounds(true);
        characterView.setScaleX(1.0f);
        characterView.setScaleY(1.0f);
        characterView.addOnLayoutChangeListener(
                (v, l, t, r, b, ol, ot, or, ob) -> updatePivot());
        characterView.post(this::updatePivot);
        if (shadowView != null) {
            shadowView.addOnLayoutChangeListener(
                    (v, l, t, r, b, ol, ot, or, ob) -> updatePivot());
        }
        setShadow(1.0f, 1.0f, 0);

        playIdleOrFat();
        startBreathingAfter(0);
    }

    // ---------------------------------------------------------------- หายใจ

    private float baseScale() {
        return 1.0f + SCALE_PER_LEVEL * Math.min(fatLevel, MAX_SCALE_LEVEL);
    }

    private void stopBreathing() {
        if (breathStarter != null) {
            handler.removeCallbacks(breathStarter);
            breathStarter = null;
        }
        if (breathing != null) {
            breathing.cancel();
            breathing = null;
        }
    }

    /** เริ่มหายใจหลังหน่วงเวลา (รอให้ Animation ขยายตัวจบก่อน) */
    private void startBreathingAfter(long delayMs) {
        if (BREATH_AMOUNT <= 0f) return;
        stopBreathing();
        breathStarter = new Runnable() {
            @Override
            public void run() {
                breathStarter = null;
                if (isDead || isBusy) return;
                final float base = baseScale();
                characterView.setScaleX(base);
                characterView.setScaleY(base);
                breathing = ValueAnimator.ofFloat(0f, 1f);
                breathing.setDuration(BREATH_MS);
                breathing.setRepeatCount(ValueAnimator.INFINITE);
                breathing.setRepeatMode(ValueAnimator.REVERSE);
                breathing.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator a) {
                        float t = (Float) a.getAnimatedValue();
                        characterView.setScaleY(base * (1f + BREATH_AMOUNT * t));
                        characterView.setScaleX(base * (1f - BREATH_AMOUNT * 0.3f * t));
                    }
                });
                breathing.start();
            }
        };
        handler.postDelayed(breathStarter, delayMs);
    }

    // ---------------------------------------------------------------- ขนาดตัวละคร

    /** ให้ตัวละครขยายโดยเท้าอยู่กับที่ (pivot อยู่ที่ตำแหน่งเท้าและกึ่งกลางภาพ) */
    private void updatePivot() {
        int w = characterView.getWidth(), h = characterView.getHeight();
        if (w == 0 || h == 0) return;
        float s = Math.min(w / SHEET_W, h / SHEET_H);        // fitCenter
        float top = (h - SHEET_H * s) / 2f;
        float feetY = top + FEET_Y * s;
        characterView.setPivotX(w / 2f);
        characterView.setPivotY(feetY);

        // วางเงาให้อยู่ใต้เท้า
        if (shadowView != null && shadowView.getWidth() > 0) {
            shadowView.setX(characterView.getLeft() + w / 2f - shadowView.getWidth() / 2f);
            shadowView.setY(characterView.getTop() + feetY - shadowView.getHeight() / 2f);
        }
    }

    /** ขนาดเงา: ยิ่งอ้วนยิ่งกว้าง (ตัวปกติ 1.0, อ้วนระดับ 1 = 1.25, ระดับ 2 = 1.45, ระดับ 3+ = 1.65) */
    private float shadowFactor() {
        return fatLevel <= 0 ? 1.0f : 1.25f + 0.2f * (Math.min(fatLevel, 3) - 1);
    }

    /** เปลี่ยนขนาดเงา durMs = 0 คือเปลี่ยนทันที */
    private void setShadow(float sx, float alpha, long durMs) {
        if (shadowView == null) return;
        shadowView.animate().cancel();
        if (durMs <= 0) {
            shadowView.setScaleX(sx);
            shadowView.setScaleY(sx * 0.9f);
            shadowView.setAlpha(alpha);
        } else {
            shadowView.animate().scaleX(sx).scaleY(sx * 0.9f).alpha(alpha)
                    .setDuration(durMs).start();
        }
    }

    private void applyScale(boolean animate) {
        final float scale = 1.0f + SCALE_PER_LEVEL * Math.min(fatLevel, MAX_SCALE_LEVEL);
        setShadow(scale * shadowFactor(), 1.0f, animate ? 110 + SCALE_ANIM_MS : 0);
        if (animate) {
            // บีบตัวนิดหนึ่ง (squash) แล้วเด้งขยายเกินเป้าเล็กน้อยก่อนนิ่ง (stretch)
            characterView.animate().cancel();
            characterView.animate()
                    .scaleX(scale * 1.12f).scaleY(scale * 0.92f)
                    .setDuration(110)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            characterView.animate()
                                    .scaleX(scale).scaleY(scale)
                                    .setInterpolator(new OvershootInterpolator(3f))
                                    .setDuration(SCALE_ANIM_MS)
                                    .start();
                        }
                    })
                    .start();
        } else {
            characterView.animate().cancel();
            characterView.setScaleX(scale);
            characterView.setScaleY(scale);
        }
    }

    // ---------------------------------------------------------------- โหลดภาพ

    /** แบ่ง Sprite Sheet เป็น 7 เฟรมเท่ากัน (ภาพถูกจัดมาให้ช่องละเท่ากันแล้ว) */
    private BitmapDrawable[] loadFrames(int resourceId) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;   // ห้ามย่อ/ขยายตอนโหลด เพื่อรักษาความคมของ Pixel Art

        Bitmap sheet = BitmapFactory.decodeResource(
                characterView.getResources(), resourceId, options);

        if (sheet == null) {
            throw new IllegalArgumentException("ไม่พบรูปภาพ resource: " + resourceId);
        }

        int frameWidth = sheet.getWidth() / FRAME_COUNT;
        if (frameWidth * FRAME_COUNT != sheet.getWidth()) {
            throw new IllegalStateException(
                    "ความกว้าง Sprite Sheet ต้องหาร 7 ลงตัว แต่ได้ " + sheet.getWidth());
        }

        BitmapDrawable[] frames = new BitmapDrawable[FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) {
            Bitmap frame = Bitmap.createBitmap(
                    sheet, i * frameWidth, 0, frameWidth, sheet.getHeight());

            BitmapDrawable drawable = new BitmapDrawable(characterView.getResources(), frame);
            drawable.setFilterBitmap(false);    // nearest-neighbor: ไม่เบลอ Pixel Art
            frames[i] = drawable;
        }
        sheet.recycle();
        return frames;
    }

    // ---------------------------------------------------------------- เล่น Animation

    private void playFrames(final BitmapDrawable[] frames, final int delay,
                            final boolean loop, final Runnable onEnd) {

        stopAnimation();

        final int[] index = {0};

        animationRunnable = new Runnable() {
            @Override
            public void run() {
                characterView.setImageDrawable(frames[index[0]]);
                index[0]++;

                if (index[0] >= frames.length) {
                    if (loop) {
                        index[0] = 0;
                        handler.postDelayed(this, delay);
                    } else {
                        // จบแล้วค้างที่เฟรมสุดท้าย
                        animationRunnable = null;
                        if (onEnd != null) {
                            onEnd.run();
                        }
                    }
                } else {
                    handler.postDelayed(this, delay);
                }
            }
        };

        handler.post(animationRunnable);
    }

    private void stopAnimation() {
        if (animationRunnable != null) {
            handler.removeCallbacks(animationRunnable);
            animationRunnable = null;
        }
    }

    /** ระดับ 0 = Idle วนซ้ำ, ระดับ 1+ = ร่างอ้วนตามระดับ (วนซ้ำ) */
    private void playIdleOrFat() {
        if (fatLevel <= 0) {
            playFrames(idleFrames, IDLE_DELAY, true, null);
        } else {
            playFrames(fatFrames[visualFatIndex()], FAT_DELAY, true, null);
        }
    }

    private int visualFatIndex() {
        return Math.min(fatLevel, fatFrames.length) - 1;
    }

    // ---------------------------------------------------------------- เหตุการณ์จากเกม

    /**
     * ตอบถูก: เล่น Eating แล้วแสดงร่างอ้วนตามระดับที่สะสม
     *
     * @return true ถ้ารับคำตอบ, false ถ้าตัวละครตายแล้ว
     */
    public boolean onCorrectAnswer() {

        if (!canAnswer()) return false;

        // เลือกภาพกินให้ตรงกับรูปร่างปัจจุบัน (ก่อนอ้วนขึ้น)
        stopBreathing();
        BitmapDrawable[] eating = eatingFrames[Math.min(fatLevel, eatingFrames.length - 1)];

        fatLevel++;     // สะสมต่อเนื่อง ไม่รีเซ็ต
        isBusy = true;
        notifyInput();

        playFrames(eating, EATING_DELAY, false, new Runnable() {
            @Override
            public void run() {
                isBusy = false;
                if (!isDead) {
                    applyScale(true);
                    effects.sparkleBurst();     // ประกายรอบตัว
                    playIdleOrFat();
                    startBreathingAfter(110 + SCALE_ANIM_MS + 50);   // หายใจหลังขยายตัวเสร็จ
                    notifyInput();
                }
            }
        });
        return true;
    }

    /**
     * ตอบผิด: ผอมทันที -> เล่น Dead -> ค้างเฟรมสุดท้าย (ไม่กลับไป Idle)
     *
     * @return true ถ้ารับคำตอบ, false ถ้าตัวละครตายแล้ว
     */
    public boolean onWrongAnswer() {

        if (!canAnswer()) return false;

        isDead = true;      // ตั้งทันที เพื่อไม่ให้รับคำตอบต่อ
        stopBreathing();
        notifyInput();      // ปิดปุ่มทันที
        // ถ้าอ้วนอยู่: ค่อย ๆ ยุบลงทีละระดับพร้อมหดสเกล แล้วค่อยเล่น Skinny (ไม่หดกระโดด)
        characterView.animate().cancel();
        final Runnable startSkinny = new Runnable() {
            @Override
            public void run() {
                characterView.setScaleX(1.0f);
                characterView.setScaleY(1.0f);
                setShadow(0.9f, 1.0f, 0);   // ผอม: เงาแคบลง
                playFrames(skinnyFrames, SKINNY_DELAY, false, new Runnable() {
                    @Override
                    public void run() {
                        playDead();
                    }
                });
            }
        };

        final Runnable afterShake = new Runnable() {
            @Override
            public void run() {
                startReaction(startSkinny);
            }
        };
        playHitEffect(afterShake);
        return true;
    }

    /** เริ่มปฏิกิริยาหลังสั่น: ยุบจากร่างอ้วนทีละระดับ (ถ้าอ้วน) แล้วเล่น Skinny */
    private void startReaction(final Runnable startSkinny) {
        if (fatLevel <= 0) {
            startSkinny.run();
        } else {
            int top = visualFatIndex();
            BitmapDrawable[] deflate = new BitmapDrawable[top + 1];
            for (int i = 0; i <= top; i++) {
                deflate[i] = fatFrames[top - i][0];     // อ้วนสุด -> อ้วนระดับ 1
            }
            characterView.animate().scaleX(1.0f).scaleY(1.0f)
                    .setDuration(DEFLATE_DELAY * (top + 1)).start();
            setShadow(1.0f, 1.0f, DEFLATE_DELAY * (top + 1));
            playFrames(deflate, DEFLATE_DELAY, false, startSkinny);
        }
    }

    /** ตอบผิด: ตัวสั่นซ้ายขวาและกระพริบแดงสั้น ๆ แล้วทำงานต่อ (translation กลับมาที่ 0 เสมอ) */
    private void playHitEffect(final Runnable onDone) {
        float d = characterView.getResources().getDisplayMetrics().density;
        characterView.setColorFilter(0x66FF0000, PorterDuff.Mode.SRC_ATOP);
        ObjectAnimator shake = ObjectAnimator.ofFloat(characterView, "translationX",
                0f, -8f * d, 8f * d, -6f * d, 6f * d, -3f * d, 3f * d, 0f);
        shake.setDuration(SHAKE_MS);
        shake.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                characterView.setTranslationX(0f);
                characterView.clearColorFilter();
                onDone.run();
            }
        });
        shake.start();
    }

    /** เล่น Dead พร้อมล้มกระแทกพื้นและเด้งเล็กน้อย แล้วค้างเฟรมสุดท้าย */
    private void playDead() {
        float d = characterView.getResources().getDisplayMetrics().density;
        characterView.setTranslationY(-28f * d);
        characterView.animate().translationY(0f)
                .setInterpolator(new BounceInterpolator())
                .setDuration(FALL_MS)
                .start();
        if (shadowView != null) {
            // ตอนตกลงมาเงาเล็กและจาง แล้วขยายเต็มเมื่อถึงพื้น (นอนอยู่ จึงกว้างกว่าตอนยืน)
            setShadow(0.8f, 0.3f, 0);
            shadowView.animate().scaleX(1.7f).scaleY(1.7f * 0.9f).alpha(1.0f)
                    .setInterpolator(new BounceInterpolator())
                    .setDuration(FALL_MS).start();
        }
        // ฝุ่นฟุ้งตอนกระแทกพื้นครั้งแรก และครั้งที่สองที่เบากว่าตอนกระดอน
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                effects.dustPuff(70, 8);
            }
        }, (long) (FALL_MS * 0.36f));
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                effects.dustPuff(40, 4);
            }
        }, (long) (FALL_MS * 0.74f));
        playFrames(deadFrames, DEAD_DELAY, false, null);
    }

    /** เริ่มเกมใหม่ (ถ้าต้องการ) */
    public void reset() {
        stopAnimation();
        stopBreathing();
        characterView.animate().cancel();
        characterView.setTranslationX(0f);
        characterView.setTranslationY(0f);
        characterView.clearColorFilter();
        setShadow(1.0f, 1.0f, 0);
        fatLevel = 0;
        isDead = false;
        isBusy = false;
        applyScale(false);
        playIdleOrFat();
        startBreathingAfter(0);
        notifyInput();
    }

    public int getFatLevel() {
        return fatLevel;
    }

    public boolean isDead() {
        return isDead;
    }

    public void release() {
        stopBreathing();
        stopAnimation();
        handler.removeCallbacksAndMessages(null);
    }
}