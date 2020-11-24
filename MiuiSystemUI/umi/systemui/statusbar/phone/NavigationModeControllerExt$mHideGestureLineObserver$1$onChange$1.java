package com.android.systemui.statusbar.phone;

/* compiled from: NavigationModeControllerExt.kt */
final class NavigationModeControllerExt$mHideGestureLineObserver$1$onChange$1 implements Runnable {
    public static final NavigationModeControllerExt$mHideGestureLineObserver$1$onChange$1 INSTANCE = new NavigationModeControllerExt$mHideGestureLineObserver$1$onChange$1();

    NavigationModeControllerExt$mHideGestureLineObserver$1$onChange$1() {
    }

    public final void run() {
        NavigationModeControllerExt.INSTANCE.onGestureLineSettingChange();
    }
}
