package com.android.systemui.recents;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import com.android.systemui.SysUiServiceProvider;

interface RecentsImplementation {
    void cancelPreloadRecentApps();

    boolean dockTopTask(int i, int i2, Rect rect, int i3);

    void hideRecentApps(boolean z, boolean z2);

    void onBootCompleted();

    void onConfigurationChanged(Configuration configuration);

    void onStart(Context context, SysUiServiceProvider sysUiServiceProvider);

    void preloadRecentApps();

    void release();

    void showRecentApps(boolean z, boolean z2);

    void toggleRecentApps();
}
