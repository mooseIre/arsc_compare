package com.android.systemui.controls.controller;

import java.util.function.Consumer;

/* compiled from: ControlsControllerImpl.kt */
final class ControlsControllerImpl$addSeedingFavoritesCallback$1 implements Runnable {
    final /* synthetic */ Consumer $callback;
    final /* synthetic */ ControlsControllerImpl this$0;

    ControlsControllerImpl$addSeedingFavoritesCallback$1(ControlsControllerImpl controlsControllerImpl, Consumer consumer) {
        this.this$0 = controlsControllerImpl;
        this.$callback = consumer;
    }

    public final void run() {
        if (ControlsControllerImpl.access$getSeedingInProgress$p(this.this$0)) {
            ControlsControllerImpl.access$getSeedingCallbacks$p(this.this$0).add(this.$callback);
        } else {
            this.$callback.accept(Boolean.FALSE);
        }
    }
}
