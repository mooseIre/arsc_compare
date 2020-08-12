package com.android.systemui.glwallpaper;

import android.os.Handler;
import android.os.Message;

public class ImageProcessHelper {
    private static final float[] LUMINOSITY_MATRIX = {0.2126f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.7152f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0722f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f};
    private final Handler mHandler = new Handler(new Handler.Callback() {
        public boolean handleMessage(Message message) {
            if (message.what != 1) {
                return false;
            }
            float unused = ImageProcessHelper.this.mThreshold = ((Float) message.obj).floatValue();
            return true;
        }
    });
    /* access modifiers changed from: private */
    public float mThreshold = 0.8f;

    public float getThreshold() {
        return Math.min(this.mThreshold, 0.89f);
    }
}
