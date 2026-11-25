package com.example.englisheatleak;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.google.android.material.button.MaterialButton;

public class OutlineButton extends MaterialButton {
    private final OutlineHelper outline;

    public OutlineButton(Context c) { this(c, null); }
    public OutlineButton(Context c, AttributeSet a) {
        this(c, a, com.google.android.material.R.attr.materialButtonStyle);
    }
    public OutlineButton(Context c, AttributeSet a, int d) {
        super(c, a, d);
        outline = new OutlineHelper(this, c, a);
    }

    @Override protected void onDraw(Canvas canvas) {
        outline.draw(canvas, () -> super.onDraw(canvas));
    }

    @Override public void invalidate() {
        if (outline != null && outline.drawing) return;
        super.invalidate();
    }
}