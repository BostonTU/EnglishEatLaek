package com.example.englisheatleak;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

final class OutlineHelper {
    private static final float WIDTH_RATIO = 0.22f;
    private static final float DEFAULT_MAX_DP = 4f;

    private final TextView view;
    private boolean enabled = true;
    private boolean hasCustomColor;
    private int customColor;
    private float maxWidthPx;
    private boolean hasShadow;
    private int shadowColor;
    private float shadowDx, shadowDy;
    boolean drawing;

    OutlineHelper(TextView view, Context ctx, AttributeSet attrs) {
        this.view = view;
        maxWidthPx = DEFAULT_MAX_DP * ctx.getResources().getDisplayMetrics().density;
        if (attrs != null) {
            TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.OutlineText);
            enabled = a.getBoolean(R.styleable.OutlineText_outlineEnabled, true);
            if (a.hasValue(R.styleable.OutlineText_outlineColor)) {
                hasCustomColor = true;
                customColor = a.getColor(R.styleable.OutlineText_outlineColor, 0);
            }
            maxWidthPx = a.getDimension(R.styleable.OutlineText_outlineMaxWidth, maxWidthPx);
            if (a.hasValue(R.styleable.OutlineText_pixelShadowColor)) {
                hasShadow = true;
                shadowColor = a.getColor(R.styleable.OutlineText_pixelShadowColor, Color.BLACK);
                shadowDx = a.getDimension(R.styleable.OutlineText_pixelShadowDx, 0f);
                shadowDy = a.getDimension(R.styleable.OutlineText_pixelShadowDy, 0f);
            }
            a.recycle();
        }

        // ขยายพื้นที่ clip ของ TextView ไม่ให้ขอบ/เงาโดนตัด (shadow โปร่งใส)
        view.setShadowLayer(maxWidthPx, hasShadow ? shadowDx : 0f, hasShadow ? shadowDy : 0f,
                Color.TRANSPARENT);

        // เผื่อที่ให้ขอบ และเงา (เงาเผื่อด้านขวา/ล่าง)
        if (enabled) {
            int pad = (int) Math.ceil(maxWidthPx / 2f);
            int extraEnd = hasShadow ? (int) Math.ceil(Math.max(0f, shadowDx)) : 0;
            int extraBottom = hasShadow ? (int) Math.ceil(Math.max(0f, shadowDy)) : 0;
            view.setPaddingRelative(
                    view.getPaddingStart() + pad, view.getPaddingTop() + pad,
                    view.getPaddingEnd() + pad + extraEnd,
                    view.getPaddingBottom() + pad + extraBottom);
        }
    }

    private int strokeColor(int fill) {
        if (hasCustomColor) return customColor;
        boolean lightText = ColorUtils.calculateLuminance(fill | 0xFF000000) > 0.5;
        return ContextCompat.getColor(view.getContext(),
                lightText ? R.color.px_stroke_dark : R.color.px_stroke_light);
    }

    void draw(Canvas canvas, Runnable superDraw) {
        float width = Math.min(maxWidthPx, view.getTextSize() * WIDTH_RATIO);
        if (!enabled || width <= 0.5f) {
            superDraw.run();
            return;
        }
        ColorStateList original = view.getTextColors();
        int fill = view.getCurrentTextColor();
        Paint paint = view.getPaint();

        drawing = true;
        try {
            // รอบ 0: เงาพิกเซล (ตัวอักษร+ขอบ เยื้องไปทางขวาล่าง)
            if (hasShadow) {
                canvas.save();
                canvas.translate(shadowDx, shadowDy);
                view.setTextColor(shadowColor);
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                paint.setStrokeJoin(Paint.Join.ROUND);
                paint.setStrokeWidth(width);
                superDraw.run();
                canvas.restore();
            }

            // รอบ 1: ขอบ
            view.setTextColor(strokeColor(fill));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(width);
            superDraw.run();

            // รอบ 2: ตัวอักษรทับ
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(0f);
            view.setTextColor(original);
            superDraw.run();
        } finally {
            paint.setStyle(Paint.Style.FILL);
            drawing = false;
        }
    }
}