package com.android.systemui.util.time;

import android.os.SystemClock;

public class SystemClockImpl implements SystemClock {
    @Override // com.android.systemui.util.time.SystemClock
    public long uptimeMillis() {
        return SystemClock.uptimeMillis();
    }

    @Override // com.android.systemui.util.time.SystemClock
    public long elapsedRealtime() {
        return SystemClock.elapsedRealtime();
    }

    @Override // com.android.systemui.util.time.SystemClock
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
