package com.android.systemui.recents;

/* compiled from: MiuiOverviewProxy.kt */
final class MiuiOverviewProxy$exitSplitScreen$1 implements Runnable {
    final /* synthetic */ MiuiOverviewProxy this$0;

    MiuiOverviewProxy$exitSplitScreen$1(MiuiOverviewProxy miuiOverviewProxy) {
        this.this$0 = miuiOverviewProxy;
    }

    public final void run() {
        this.this$0.proxyService.getDividerOptional().get().onUndockingTask();
    }
}
