package com.android.systemui.controls.controller;

import com.android.systemui.controls.controller.ControlsBindingControllerImpl;

/* access modifiers changed from: package-private */
/* compiled from: ControlsBindingControllerImpl.kt */
public final class ControlsBindingControllerImpl$LoadSubscriber$maybeTerminateAndRun$2 implements Runnable {
    final /* synthetic */ Runnable $postTerminateFn;
    final /* synthetic */ ControlsBindingControllerImpl.LoadSubscriber this$0;

    ControlsBindingControllerImpl$LoadSubscriber$maybeTerminateAndRun$2(ControlsBindingControllerImpl.LoadSubscriber loadSubscriber, Runnable runnable) {
        this.this$0 = loadSubscriber;
        this.$postTerminateFn = runnable;
    }

    public final void run() {
        this.this$0.isTerminated.compareAndSet(false, true);
        this.$postTerminateFn.run();
    }
}
