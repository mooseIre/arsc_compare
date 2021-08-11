package com.android.systemui.recents;

/* compiled from: MiuiOverviewProxy.kt */
final class MiuiOverviewProxy$onGestureLineProgress$1 implements Runnable {
    final /* synthetic */ float $progress;
    final /* synthetic */ MiuiOverviewProxy this$0;

    MiuiOverviewProxy$onGestureLineProgress$1(MiuiOverviewProxy miuiOverviewProxy, float f) {
        this.this$0 = miuiOverviewProxy;
        this.$progress = f;
    }

    public final void run() {
        this.this$0.notifyGestureLineProgress(this.$progress);
    }
}
