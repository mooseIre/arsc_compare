package com.android.systemui.statusbar.phone;

import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

/* compiled from: NavigationModeControllerExt.kt */
public final class NavigationModeControllerExt$mHideGestureLineObserver$1 extends ContentObserver {
    NavigationModeControllerExt$mHideGestureLineObserver$1(Handler handler) {
        super(handler);
    }

    public void onChange(boolean z) {
        if (!NavigationModeControllerExt.INSTANCE.getMIsFsgMode()) {
            Log.w("NavigationModeControllerExt", " not is fsg mode");
            return;
        }
        boolean z2 = false;
        if (Settings.Global.getInt(NavigationModeControllerExt.access$getMContext$p(NavigationModeControllerExt.INSTANCE).getContentResolver(), "hide_gesture_line", 0) != 0) {
            z2 = true;
        }
        NavigationModeControllerExt navigationModeControllerExt = NavigationModeControllerExt.INSTANCE;
        if (z2 != NavigationModeControllerExt.mHideGestureLine) {
            NavigationModeControllerExt navigationModeControllerExt2 = NavigationModeControllerExt.INSTANCE;
            NavigationModeControllerExt.mHideGestureLine = z2;
            new Handler(Looper.getMainLooper()).post(NavigationModeControllerExt$mHideGestureLineObserver$1$onChange$1.INSTANCE);
        }
    }
}
