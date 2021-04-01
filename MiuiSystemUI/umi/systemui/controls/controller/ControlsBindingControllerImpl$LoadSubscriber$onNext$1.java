package com.android.systemui.controls.controller;

import android.os.IBinder;
import android.service.controls.Control;
import com.android.systemui.controls.controller.ControlsBindingControllerImpl;

/* compiled from: ControlsBindingControllerImpl.kt */
final class ControlsBindingControllerImpl$LoadSubscriber$onNext$1 implements Runnable {
    final /* synthetic */ Control $c;
    final /* synthetic */ IBinder $token;
    final /* synthetic */ ControlsBindingControllerImpl.LoadSubscriber this$0;

    ControlsBindingControllerImpl$LoadSubscriber$onNext$1(ControlsBindingControllerImpl.LoadSubscriber loadSubscriber, Control control, IBinder iBinder) {
        this.this$0 = loadSubscriber;
        this.$c = control;
        this.$token = iBinder;
    }

    public final void run() {
        if (!this.this$0.isTerminated.get()) {
            this.this$0.getLoadedControls().add(this.$c);
            if (((long) this.this$0.getLoadedControls().size()) >= this.this$0.getRequestLimit()) {
                ControlsBindingControllerImpl.LoadSubscriber loadSubscriber = this.this$0;
                loadSubscriber.maybeTerminateAndRun(new ControlsBindingControllerImpl.OnCancelAndLoadRunnable(loadSubscriber.this$0, this.$token, loadSubscriber.getLoadedControls(), ControlsBindingControllerImpl.LoadSubscriber.access$getSubscription$p(this.this$0), this.this$0.getCallback()));
            }
        }
    }
}
