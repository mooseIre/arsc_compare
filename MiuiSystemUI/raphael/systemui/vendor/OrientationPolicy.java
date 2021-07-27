package com.android.systemui.vendor;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.IWindowManager;
import android.view.WindowManager;

public class OrientationPolicy {
    private Context mContext;
    private Display mDisplay;
    private int mLastRotation = -1;

    public OrientationPolicy(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Display defaultDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        this.mDisplay = defaultDisplay;
        defaultDisplay.getRealMetrics(displayMetrics);
        this.mContext = context;
    }

    public void start() {
        boolean z;
        try {
            z = IWindowManager.Stub.asInterface(ServiceManager.getService("window")).hasNavigationBar(this.mContext.getDisplayId());
        } catch (RemoteException unused) {
            z = false;
        }
        if (z) {
            ((DisplayManager) this.mContext.getSystemService("display")).registerDisplayListener(new CustomDisplayListener(), null);
            writeRotationForBsp();
        }
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
    /* access modifiers changed from: public */
    private void writeRotationForBsp() {
        int rotation = this.mDisplay.getRotation();
        final int i = rotation != 0 ? rotation != 1 ? rotation != 2 ? rotation != 3 ? -1 : 270 : 180 : 90 : 0;
        if (this.mLastRotation != i) {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable(this) {
                /* class com.android.systemui.vendor.OrientationPolicy.AnonymousClass1 */

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
