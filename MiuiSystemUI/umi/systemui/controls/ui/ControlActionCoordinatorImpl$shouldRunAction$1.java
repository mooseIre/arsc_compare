package com.android.systemui.controls.ui;

/* compiled from: ControlActionCoordinatorImpl.kt */
final class ControlActionCoordinatorImpl$shouldRunAction$1 implements Runnable {
    final /* synthetic */ String $controlId;
    final /* synthetic */ ControlActionCoordinatorImpl this$0;

    ControlActionCoordinatorImpl$shouldRunAction$1(ControlActionCoordinatorImpl controlActionCoordinatorImpl, String str) {
        this.this$0 = controlActionCoordinatorImpl;
        this.$controlId = str;
    }

    public final void run() {
        ControlActionCoordinatorImpl.access$getActionsInProgress$p(this.this$0).remove(this.$controlId);
    }
}
