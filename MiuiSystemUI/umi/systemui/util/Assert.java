package com.android.systemui.util;

import android.os.Looper;

public class Assert {
    private static final Looper sMainLooper = Looper.getMainLooper();
    private static Looper sTestLooper = null;

    public static void setTestableLooper(Looper looper) {
        sTestLooper = looper;
    }

    public static void isMainThread() {
        if (!sMainLooper.isCurrentThread()) {
            Looper looper = sTestLooper;
            if (looper == null || !looper.isCurrentThread()) {
                throw new IllegalStateException("should be called from the main thread. sMainLooper.threadName=" + sMainLooper.getThread().getName() + " Thread.currentThread()=" + Thread.currentThread().getName());
            }
        }
    }
}
