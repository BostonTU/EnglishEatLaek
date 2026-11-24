package com.example.englisheatleak;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

/** เล่น Animation ตัวละครกำลังกินวนซ้ำบนหน้าแรก (ใช้ Sprite Sheet eating_7_frames) */
public class StartScreenAnimator {

    private static final int FRAME_COUNT = 7;
    private static final int FRAME_DELAY_MS = 110;
    private static final int LOOP_PAUSE_MS = 350;   // หยุดสั้น ๆ ก่อนกินรอบใหม่

    private final ImageView view;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final BitmapDrawable[] frames;
    private Runnable runnable;

    public StartScreenAnimator(ImageView view) {
        this.view = view;
        this.frames = loadFrames(R.drawable.eating_7_frames);
        view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        view.setImageDrawable(frames[0]);
    }

    public void start() {
        stop();
        final int[] index = {0};
        runnable = new Runnable() {
            @Override
            public void run() {
                view.setImageDrawable(frames[index[0]]);
                index[0]++;
                long delay = FRAME_DELAY_MS;
                if (index[0] >= FRAME_COUNT) {
                    index[0] = 0;
                    delay = LOOP_PAUSE_MS;
                }
                handler.postDelayed(this, delay);
            }
        };
        handler.post(runnable);
    }

    public void stop() {
        if (runnable != null) {
            handler.removeCallbacks(runnable);
            runnable = null;
        }
    }

    public void release() {
        stop();
        handler.removeCallbacksAndMessages(null);
    }

    private BitmapDrawable[] loadFrames(int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap sheet = BitmapFactory.decodeResource(view.getResources(), resId, options);
        if (sheet == null) {
            throw new IllegalArgumentException("ไม่พบรูปภาพ resource: " + resId);
        }
        int w = sheet.getWidth() / FRAME_COUNT;
        BitmapDrawable[] out = new BitmapDrawable[FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) {
            Bitmap f = Bitmap.createBitmap(sheet, i * w, 0, w, sheet.getHeight());
            BitmapDrawable d = new BitmapDrawable(view.getResources(), f);
            d.setFilterBitmap(false);   // คม ไม่เบลอ Pixel Art
            out[i] = d;
        }
        sheet.recycle();
        return out;
    }
}