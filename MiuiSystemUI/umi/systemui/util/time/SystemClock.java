package com.android.systemui.util.time;

public interface SystemClock {
    long currentTimeMillis();

    long elapsedRealtime();

    long uptimeMillis();
}
