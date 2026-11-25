package com.example.englisheatleak;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class OutlineTextView extends AppCompatTextView {
    private final OutlineHelper outline;

    public OutlineTextView(Context c) { this(c, null); }
    public OutlineTextView(Context c, AttributeSet a) { this(c, a, android.R.attr.textViewStyle); }
    public OutlineTextView(Context c, AttributeSet a, int d) {
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