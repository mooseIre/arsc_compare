package com.android.systemui.statusbar.phone;

/* compiled from: NavigationModeControllerExt.kt */
final class NavigationModeControllerExt$mFullScreenGestureListener$1$onChange$1 implements Runnable {
    public static final NavigationModeControllerExt$mFullScreenGestureListener$1$onChange$1 INSTANCE = new NavigationModeControllerExt$mFullScreenGestureListener$1$onChange$1();

    NavigationModeControllerExt$mFullScreenGestureListener$1$onChange$1() {
    }

    public final void run() {
        NavigationModeControllerExt.INSTANCE.onFsGestureStateChange();
    }
}
