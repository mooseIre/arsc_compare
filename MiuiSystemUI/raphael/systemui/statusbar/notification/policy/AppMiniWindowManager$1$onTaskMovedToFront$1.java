package com.android.systemui.statusbar.notification.policy;

import com.android.systemui.statusbar.notification.policy.AppMiniWindowManager;

/* compiled from: AppMiniWindowManager.kt */
final class AppMiniWindowManager$1$onTaskMovedToFront$1 implements Runnable {
    final /* synthetic */ AppMiniWindowManager.AnonymousClass1 this$0;

    AppMiniWindowManager$1$onTaskMovedToFront$1(AppMiniWindowManager.AnonymousClass1 r1) {
        this.this$0 = r1;
    }

    public final void run() {
        this.this$0.this$0.updateAllHeadsUpMiniBars();
    }
}
