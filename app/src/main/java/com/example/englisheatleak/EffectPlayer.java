package com.example.englisheatleak;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import java.util.Random;

/**
 * เอฟเฟกต์ลอยเหนือตัวละคร (ข้อความ, ประกาย, ฝุ่น) สร้างด้วยโค้ดล้วน ไม่ต้องใช้ภาพ
 * View ทุกตัวถูกเพิ่มเข้า parent ของ characterView ชั่วคราวและลบทิ้งเองเมื่อเล่นจบ
 */
public class EffectPlayer {

    // ตำแหน่งใน Canvas ของ Sprite (px) ต้องตรงกับ CharacterController
    private static final float SHEET_W = 240f, SHEET_H = 224f;
    private static final float FEET_Y = 214f, HEAD_Y = 8f, BODY_Y = 125f;

    private static final String[] WORDS = {"Let's go", "JamesEatLaek", "WOOHOO", "YUMMY !"};
    private static final int[] WORD_COLORS = {0xFFFFE04A, 0xFFFF8FB1, 0xFF7CF29C, 0xFFFFA64D};
    private static final String[] SPARKS = {"✦", "★", "✧"};
    private static final int[] SPARK_COLORS = {0xFFFFE04A, 0xFFFFFFFF, 0xFFFFB347, 0xFFFF8FB1};

    private final View anchor;
    private final Random random = new Random();
    private int lastWord = -1;

    public EffectPlayer(View anchor) {
        this.anchor = anchor;
    }

    // ---------------------------------------------------------------- ตำแหน่ง

    private ViewGroup parent() {
        return anchor.getParent() instanceof ViewGroup ? (ViewGroup) anchor.getParent() : null;
    }

    private float dp(float v) {
        return v * anchor.getResources().getDisplayMetrics().density;
    }

    private float fit() {
        return Math.min(anchor.getWidth() / SHEET_W, anchor.getHeight() / SHEET_H);
    }

    private float centerX() {
        return anchor.getLeft() + anchor.getWidth() / 2f + anchor.getTranslationX();
    }

    /** y ใน parent ของจุด sheetY (พิกเซลบน Sprite) โดยรวมสเกลที่ขยายจากเท้าและการเลื่อนอยู่ */
    private float pointY(float sheetY, boolean includeTransform) {
        float s = fit();
        float top = (anchor.getHeight() - SHEET_H * s) / 2f;
        float feet = top + FEET_Y * s;
        float y = top + sheetY * s;
        if (includeTransform) {
            y = feet + (y - feet) * anchor.getScaleY() + anchor.getTranslationY();
        }
        return anchor.getTop() + y;
    }

    // ---------------------------------------------------------------- ข้อความลอย

    /** ข้อความสั้น ๆ เด้งขึ้นเหนือหัวแล้วลอยหายไป (สุ่มคำ ไม่ซ้ำคำเดิมติดกัน) */
    public void showWord() {
        int i;
        do {
            i = random.nextInt(WORDS.length);
        } while (i == lastWord);
        lastWord = i;
        showCustomWord(WORDS[i], WORD_COLORS[i]);
    }

    /** แสดงข้อความลอยแบบกำหนดคำและสีเอง */
    public void showCustomWord(String text, int color) {
        ViewGroup parent = parent();
        if (parent == null || anchor.getWidth() == 0) return;

        final TextView tv = new TextView(anchor.getContext());
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextColor(color);
        tv.setShadowLayer(dp(3), 0f, dp(1), 0xFF000000);
        tv.setAlpha(0f);
        parent.addView(tv, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        tv.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        float w = tv.getMeasuredWidth(), h = tv.getMeasuredHeight();
        float margin = dp(8);
        float x = Math.max(margin, Math.min(parent.getWidth() - w - margin, centerX() - w / 2f));
        float y = pointY(HEAD_Y, true) - h - dp(6);

        tv.setX(x);
        tv.setY(y);
        tv.setPivotX(w / 2f);
        tv.setPivotY(h);
        tv.setScaleX(0.4f);
        tv.setScaleY(0.4f);

        final ViewGroup p = parent;
        final float rise = dp(55);
        tv.animate().alpha(1f).scaleX(1f).scaleY(1f)
                .setInterpolator(new OvershootInterpolator(3f))
                .setDuration(260)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        tv.animate().translationYBy(-rise).alpha(0f)
                                .setInterpolator(new DecelerateInterpolator())
                                .setStartDelay(450)
                                .setDuration(750)
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        p.removeView(tv);
                                    }
                                })
                                .start();
                    }
                })
                .start();
    }

    // ---------------------------------------------------------------- ประกาย

    /** ประกายพุ่งออกรอบตัว ตอนกินเสร็จ */
    public void sparkleBurst() {
        final ViewGroup parent = parent();
        if (parent == null || anchor.getWidth() == 0) return;

        float cx = centerX();
        float cy = pointY(BODY_Y, true);
        int count = 10;

        for (int n = 0; n < count; n++) {
            final TextView sp = new TextView(anchor.getContext());
            sp.setText(SPARKS[random.nextInt(SPARKS.length)]);
            sp.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14 + random.nextInt(11));
            sp.setTextColor(SPARK_COLORS[random.nextInt(SPARK_COLORS.length)]);
            sp.setShadowLayer(dp(2), 0f, 0f, 0x99000000);
            parent.addView(sp, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            sp.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            float w = sp.getMeasuredWidth(), h = sp.getMeasuredHeight();
            sp.setX(cx - w / 2f);
            sp.setY(cy - h / 2f);
            sp.setScaleX(0.3f);
            sp.setScaleY(0.3f);

            double ang = (2 * Math.PI * n / count) + random.nextDouble() * 0.5;
            float dist = dp(55 + random.nextInt(45));
            sp.animate()
                    .translationXBy((float) Math.cos(ang) * dist)
                    .translationYBy((float) Math.sin(ang) * dist - dp(15))
                    .scaleX(1.2f).scaleY(1.2f)
                    .rotationBy(random.nextBoolean() ? 120f : -120f)
                    .alpha(0f)
                    .setInterpolator(new DecelerateInterpolator())
                    .setDuration(650 + random.nextInt(250))
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            parent.removeView(sp);
                        }
                    })
                    .start();
        }
    }

    // ---------------------------------------------------------------- ฝุ่น

    /** ฝุ่นฟุ้งกระจายตามพื้นตอนตัวกระแทก (spread = ความกว้างที่ฟุ้ง) */
    public void dustPuff(float spreadDp, int count) {
        final ViewGroup parent = parent();
        if (parent == null || anchor.getWidth() == 0) return;

        Context ctx = anchor.getContext();
        float cx = anchor.getLeft() + anchor.getWidth() / 2f;
        float groundY = pointY(FEET_Y, false) - dp(4);

        for (int n = 0; n < count; n++) {
            final View puff = new View(ctx);
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(0xCCE8DFD0);
            puff.setBackground(bg);

            int size = (int) dp(8 + random.nextInt(10));
            parent.addView(puff, new ViewGroup.LayoutParams(size, size));

            float side = (n % 2 == 0) ? -1f : 1f;
            float startX = cx + side * dp(random.nextInt(30)) - size / 2f;
            puff.setX(startX);
            puff.setY(groundY - size);
            puff.setAlpha(0.9f);

            puff.animate()
                    .translationXBy(side * dp(spreadDp * (0.4f + random.nextFloat() * 0.6f)))
                    .translationYBy(-dp(10 + random.nextInt(14)))
                    .scaleX(2.2f).scaleY(2.2f)
                    .alpha(0f)
                    .setInterpolator(new DecelerateInterpolator())
                    .setDuration(500 + random.nextInt(200))
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            parent.removeView(puff);
                        }
                    })
                    .start();
        }
    }
}