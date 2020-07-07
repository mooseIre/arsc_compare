package com.android.systemui.glwallpaper;

import android.os.Handler;
import android.os.Message;

public class ImageProcessHelper {
    /* access modifiers changed from: private */
    public float mThreshold = 0.8f;

    public ImageProcessHelper() {
        new Handler(new Handler.Callback() {
            public boolean handleMessage(Message message) {
                if (message.what != 1) {
                    return false;
                }
                float unused = ImageProcessHelper.this.mThreshold = ((Float) message.obj).floatValue();
                return true;
            }
        });
    }

    public float getThreshold() {
        return Math.min(this.mThreshold, 0.89f);
    }
}
