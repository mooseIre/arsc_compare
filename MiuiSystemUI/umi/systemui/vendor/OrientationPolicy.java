package com.android.systemui.vendor;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class OrientationPolicy {
    private Display mDisplay;
    private final DisplayManager mDisplayManager;
    private int mLastRotation = -1;
    private final CustomDisplayListener mOrientationDetector;

    public OrientationPolicy(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Display defaultDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        this.mDisplay = defaultDisplay;
        defaultDisplay.getRealMetrics(displayMetrics);
        this.mOrientationDetector = new CustomDisplayListener();
        this.mDisplayManager = (DisplayManager) context.getSystemService("display");
        writeRotationForBsp();
        this.mDisplayManager.registerDisplayListener(this.mOrientationDetector, (Handler) null);
    }

    private class CustomDisplayListener implements DisplayManager.DisplayListener {
        public void onDisplayAdded(int i) {
        }

        public void onDisplayRemoved(int i) {
        }

        private CustomDisplayListener() {
        }

        public void onDisplayChanged(int i) {
            OrientationPolicy.this.writeRotationForBsp();
        }
    }

    /* access modifiers changed from: private */
    public void writeRotationForBsp() {
        int rotation = this.mDisplay.getRotation();
        final int i = rotation != 0 ? rotation != 1 ? rotation != 2 ? rotation != 3 ? -1 : 270 : 180 : 90 : 0;
        if (this.mLastRotation != i) {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable(this) {
                public void run() {
                    try {
                        SystemProperties.set("sys.tp.grip_enable", Integer.toString(i));
                    } catch (Exception e) {
                        Log.e("OrientationPolicy", "set SystemProperties error.", e);
                    }
                }
            });
            this.mLastRotation = i;
        }
    }
}
