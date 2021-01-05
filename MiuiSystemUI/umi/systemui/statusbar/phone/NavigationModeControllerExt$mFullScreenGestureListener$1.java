package com.android.systemui.statusbar.phone;

import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.provider.MiuiSettings;

/* compiled from: NavigationModeControllerExt.kt */
public final class NavigationModeControllerExt$mFullScreenGestureListener$1 extends ContentObserver {
    NavigationModeControllerExt$mFullScreenGestureListener$1(Handler handler) {
        super(handler);
    }

    public void onChange(boolean z) {
        boolean z2 = MiuiSettings.Global.getBoolean(NavigationModeControllerExt.access$getMContext$p(NavigationModeControllerExt.INSTANCE).getContentResolver(), "force_fsg_nav_bar");
        if (z2 != NavigationModeControllerExt.INSTANCE.getMIsFsgMode()) {
            NavigationModeControllerExt.INSTANCE.setMIsFsgMode(z2);
            new Handler(Looper.getMainLooper()).post(NavigationModeControllerExt$mFullScreenGestureListener$1$onChange$1.INSTANCE);
        }
    }
}
