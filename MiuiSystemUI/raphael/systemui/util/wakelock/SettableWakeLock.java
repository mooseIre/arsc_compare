package com.android.systemui.util.wakelock;

import java.util.Objects;

public class SettableWakeLock {
    private boolean mAcquired;
    private final WakeLock mInner;
    private final String mWhy;

    public SettableWakeLock(WakeLock wakeLock, String str) {
        Objects.requireNonNull(wakeLock, "inner wakelock required");
        this.mInner = wakeLock;
        this.mWhy = str;
    }

    public synchronized void setAcquired(boolean z) {
        if (this.mAcquired != z) {
            if (z) {
                this.mInner.acquire(this.mWhy);
            } else {
                this.mInner.release(this.mWhy);
            }
            this.mAcquired = z;
        }
    }
}
