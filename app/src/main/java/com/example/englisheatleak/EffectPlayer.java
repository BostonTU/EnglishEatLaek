package com.example.englisheatleak;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import java.util.Random;

/**
 * เอฟเฟกต์ลอยเหนือตัวละคร (ประกาย, ฝุ่นพิกเซล) สร้างด้วยโค้ดล้วน
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

    private static final int[] SMOKE_FILLS = {
            0xFFF7F0E3, // Light smoke
            0xFFEADCC9, // Medium smoke
            0xFFD5C4AC  // Dust tone
    };
    private static final int SMOKE_OUTLINE = 0xFF4E3728; // Dark pixel outline

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

    // ---------------------------------------------------------------- ฝุ่นควันพิกเซล

    /** สร้าง Drawable ก้อนควันพิกเซลขอบเข้ม (Pixel Notched Block) */
    private Drawable createPixelSmokeDrawable(int fillColor, int outlineColor) {
        int inset = (int) Math.max(dp(2), 2);

        GradientDrawable hBar = new GradientDrawable();
        hBar.setShape(GradientDrawable.RECTANGLE);
        hBar.setColor(outlineColor);

        GradientDrawable vBar = new GradientDrawable();
        vBar.setShape(GradientDrawable.RECTANGLE);
        vBar.setColor(outlineColor);

        GradientDrawable fill = new GradientDrawable();
        fill.setShape(GradientDrawable.RECTANGLE);
        fill.setColor(fillColor);

        LayerDrawable layers = new LayerDrawable(new Drawable[]{hBar, vBar, fill});
        layers.setLayerInset(0, inset, 0, inset, 0); // horiz border
        layers.setLayerInset(1, 0, inset, 0, inset); // vert border
        layers.setLayerInset(2, inset, inset, inset, inset); // inner fill

        return layers;
    }

    /** ฝุ่นควันพิกเซลฟุ้งกระจายตามพื้นตอนตัวกระแทก (spread = ความกว้างที่ฟุ้ง) */
    public void dustPuff(float spreadDp, int count) {
        final ViewGroup parent = parent();
        if (parent == null || anchor.getWidth() == 0) return;

        Context ctx = anchor.getContext();
        float cx = anchor.getLeft() + anchor.getWidth() / 2f;
        float groundY = pointY(FEET_Y, false) - dp(4);

        for (int n = 0; n < count; n++) {
            final View puff = new View(ctx);
            int fillColor = SMOKE_FILLS[random.nextInt(SMOKE_FILLS.length)];
            puff.setBackground(createPixelSmokeDrawable(fillColor, SMOKE_OUTLINE));

            int size = (int) dp(10 + random.nextInt(12));
            parent.addView(puff, new ViewGroup.LayoutParams(size, size));

            float side = (n % 2 == 0) ? -1f : 1f;
            float startX = cx + side * dp(random.nextInt(32)) - size / 2f;
            puff.setX(startX);
            puff.setY(groundY - size);
            puff.setAlpha(0.95f);

            // หมุนเป็นมุมพิกเซล 0, 90, 180, 270 องศา
            float rot = random.nextInt(4) * 90f;
            puff.setRotation(rot);

            puff.animate()
                    .translationXBy(side * dp(spreadDp * (0.4f + random.nextFloat() * 0.6f)))
                    .translationYBy(-dp(12 + random.nextInt(16)))
                    .scaleX(2.0f).scaleY(2.0f)
                    .rotation(rot + (random.nextBoolean() ? 90f : -90f))
                    .alpha(0f)
                    .setInterpolator(new DecelerateInterpolator())
                    .setDuration(520 + random.nextInt(220))
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