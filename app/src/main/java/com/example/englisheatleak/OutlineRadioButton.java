package com.example.englisheatleak;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.google.android.material.radiobutton.MaterialRadioButton;

public class OutlineRadioButton extends MaterialRadioButton {
    private final OutlineHelper outline;

    public OutlineRadioButton(Context c) { this(c, null); }
    public OutlineRadioButton(Context c, AttributeSet a) {
        this(c, a, androidx.appcompat.R.attr.radioButtonStyle);
    }
    public OutlineRadioButton(Context c, AttributeSet a, int d) {
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