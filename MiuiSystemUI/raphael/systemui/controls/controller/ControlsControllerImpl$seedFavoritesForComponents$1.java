package com.android.systemui.controls.controller;

import java.util.List;
import java.util.function.Consumer;

/* access modifiers changed from: package-private */
/* compiled from: ControlsControllerImpl.kt */
public final class ControlsControllerImpl$seedFavoritesForComponents$1 implements Runnable {
    final /* synthetic */ Consumer $callback;
    final /* synthetic */ List $componentNames;
    final /* synthetic */ ControlsControllerImpl this$0;

    ControlsControllerImpl$seedFavoritesForComponents$1(ControlsControllerImpl controlsControllerImpl, List list, Consumer consumer) {
        this.this$0 = controlsControllerImpl;
        this.$componentNames = list;
        this.$callback = consumer;
    }

    public final void run() {
        this.this$0.seedFavoritesForComponents(this.$componentNames, this.$callback);
    }
}
