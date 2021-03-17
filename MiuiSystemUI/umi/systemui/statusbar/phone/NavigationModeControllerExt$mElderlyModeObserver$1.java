package com.android.systemui.statusbar.phone;

import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;

/* compiled from: NavigationModeControllerExt.kt */
public final class NavigationModeControllerExt$mElderlyModeObserver$1 extends ContentObserver {
    NavigationModeControllerExt$mElderlyModeObserver$1(Handler handler) {
        super(handler);
    }

    public void onChange(boolean z) {
        new Handler(Looper.getMainLooper()).post(NavigationModeControllerExt$mElderlyModeObserver$1$onChange$1.INSTANCE);
    }
}
