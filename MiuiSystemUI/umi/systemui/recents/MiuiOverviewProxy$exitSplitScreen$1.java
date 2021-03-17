package com.android.systemui.recents;

/* compiled from: MiuiOverviewProxy.kt */
final class MiuiOverviewProxy$exitSplitScreen$1 implements Runnable {
    final /* synthetic */ MiuiOverviewProxy this$0;

    MiuiOverviewProxy$exitSplitScreen$1(MiuiOverviewProxy miuiOverviewProxy) {
        this.this$0 = miuiOverviewProxy;
    }

    public final void run() {
        MiuiOverviewProxy.access$getProxyService$p(this.this$0).getDividerOptional().get().onUndockingTask();
    }
}
