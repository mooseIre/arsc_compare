package com.android.systemui.controls.controller;

import com.android.systemui.controls.controller.ControlsBindingControllerImpl;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;

/* compiled from: ControlsBindingControllerImpl.kt */
final class ControlsBindingControllerImpl$LoadSubscriber$onSubscribe$1 extends Lambda implements Function0<Unit> {
    final /* synthetic */ ControlsBindingControllerImpl.LoadSubscriber this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    ControlsBindingControllerImpl$LoadSubscriber$onSubscribe$1(ControlsBindingControllerImpl.LoadSubscriber loadSubscriber) {
        super(0);
        this.this$0 = loadSubscriber;
    }

    @Override // kotlin.jvm.functions.Function0
    public final void invoke() {
        ControlsProviderLifecycleManager access$getCurrentProvider$p = ControlsBindingControllerImpl.access$getCurrentProvider$p(this.this$0.this$0);
        if (access$getCurrentProvider$p != null) {
            access$getCurrentProvider$p.cancelSubscription(ControlsBindingControllerImpl.LoadSubscriber.access$getSubscription$p(this.this$0));
        }
    }
}
