package com.android.systemui.pip.phone;

import android.os.Handler;
import android.os.HandlerThread;

public final class PipUpdateThread extends HandlerThread {
    private static PipUpdateThread sInstance;

    private PipUpdateThread() {
        super("pip");
    }

    private static void ensureThreadLocked() {
        if (sInstance == null) {
            PipUpdateThread pipUpdateThread = new PipUpdateThread();
            sInstance = pipUpdateThread;
            pipUpdateThread.start();
            new Handler(sInstance.getLooper());
        }
    }

    public static PipUpdateThread get() {
        PipUpdateThread pipUpdateThread;
        synchronized (PipUpdateThread.class) {
            ensureThreadLocked();
            pipUpdateThread = sInstance;
        }
        return pipUpdateThread;
    }
}
