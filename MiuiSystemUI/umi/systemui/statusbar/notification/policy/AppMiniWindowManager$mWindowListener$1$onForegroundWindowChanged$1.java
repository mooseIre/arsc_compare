package com.android.systemui.statusbar.notification.policy;

/* compiled from: AppMiniWindowManager.kt */
final class AppMiniWindowManager$mWindowListener$1$onForegroundWindowChanged$1 implements Runnable {
    final /* synthetic */ AppMiniWindowManager$mWindowListener$1 this$0;

    AppMiniWindowManager$mWindowListener$1$onForegroundWindowChanged$1(AppMiniWindowManager$mWindowListener$1 appMiniWindowManager$mWindowListener$1) {
        this.this$0 = appMiniWindowManager$mWindowListener$1;
    }

    public final void run() {
        this.this$0.this$0.updateAllHeadsUpMiniBars();
    }
}
