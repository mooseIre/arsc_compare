package com.android.systemui.statusbar.phone;

/* compiled from: NavigationModeControllerExt.kt */
final class NavigationModeControllerExt$mElderlyModeObserver$1$onChange$1 implements Runnable {
    public static final NavigationModeControllerExt$mElderlyModeObserver$1$onChange$1 INSTANCE = new NavigationModeControllerExt$mElderlyModeObserver$1$onChange$1();

    NavigationModeControllerExt$mElderlyModeObserver$1$onChange$1() {
    }

    public final void run() {
        NavigationModeControllerExt.INSTANCE.onElderModeChange();
    }
}
