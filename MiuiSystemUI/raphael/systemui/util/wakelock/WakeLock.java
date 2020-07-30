package com.android.systemui.util.wakelock;

public interface WakeLock {
    void acquire();

    void release();
}
