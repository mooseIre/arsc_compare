package com.android.systemui.controls.controller;

import android.content.ComponentName;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: ControlsBindingControllerImpl.kt */
final class ControlsBindingControllerImpl$onComponentRemoved$1 implements Runnable {
    final /* synthetic */ ComponentName $componentName;
    final /* synthetic */ ControlsBindingControllerImpl this$0;

    ControlsBindingControllerImpl$onComponentRemoved$1(ControlsBindingControllerImpl controlsBindingControllerImpl, ComponentName componentName) {
        this.this$0 = controlsBindingControllerImpl;
        this.$componentName = componentName;
    }

    public final void run() {
        ControlsProviderLifecycleManager controlsProviderLifecycleManager = this.this$0.currentProvider;
        if (controlsProviderLifecycleManager != null && Intrinsics.areEqual(controlsProviderLifecycleManager.getComponentName(), this.$componentName)) {
            this.this$0.unbind();
        }
    }
}
