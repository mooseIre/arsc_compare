package com.android.systemui;

import android.graphics.Rect;

public interface RecentsComponent {
    boolean dockTopTask(int i, int i2, Rect rect, int i3);

    void showRecentApps(boolean z, boolean z2);
}
