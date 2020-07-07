package com.android.systemui.util.wakelock;

import com.android.internal.util.Preconditions;

public class SettableWakeLock {
    private boolean mAcquired;
    private final WakeLock mInner;

    public SettableWakeLock(WakeLock wakeLock) {
        Preconditions.checkNotNull(wakeLock, "inner wakelock required");
        this.mInner = wakeLock;
    }

    public synchronized void setAcquired(boolean z) {
        if (this.mAcquired != z) {
            if (z) {
                this.mInner.acquire();
            } else {
                this.mInner.release();
            }
            this.mAcquired = z;
        }
    }
}
