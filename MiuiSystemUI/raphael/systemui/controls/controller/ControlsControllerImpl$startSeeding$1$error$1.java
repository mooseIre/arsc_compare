package com.android.systemui.controls.controller;

import java.util.function.Consumer;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: ControlsControllerImpl.kt */
final class ControlsControllerImpl$startSeeding$1$error$1 implements Runnable {
    final /* synthetic */ ControlsControllerImpl$startSeeding$1 this$0;

    ControlsControllerImpl$startSeeding$1$error$1(ControlsControllerImpl$startSeeding$1 controlsControllerImpl$startSeeding$1) {
        this.this$0 = controlsControllerImpl$startSeeding$1;
    }

    public final void run() {
        ControlsControllerImpl$startSeeding$1 controlsControllerImpl$startSeeding$1 = this.this$0;
        Consumer consumer = controlsControllerImpl$startSeeding$1.$callback;
        String packageName = controlsControllerImpl$startSeeding$1.$componentName.getPackageName();
        Intrinsics.checkExpressionValueIsNotNull(packageName, "componentName.packageName");
        consumer.accept(new SeedResponse(packageName, false));
        ControlsControllerImpl$startSeeding$1 controlsControllerImpl$startSeeding$12 = this.this$0;
        controlsControllerImpl$startSeeding$12.this$0.startSeeding(controlsControllerImpl$startSeeding$12.$remaining, controlsControllerImpl$startSeeding$12.$callback, true);
    }
}
