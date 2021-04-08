package com.android.systemui.controls.ui;

/* access modifiers changed from: package-private */
/* compiled from: ControlActionCoordinatorImpl.kt */
public final class ControlActionCoordinatorImpl$shouldRunAction$1 implements Runnable {
    final /* synthetic */ String $controlId;
    final /* synthetic */ ControlActionCoordinatorImpl this$0;

    ControlActionCoordinatorImpl$shouldRunAction$1(ControlActionCoordinatorImpl controlActionCoordinatorImpl, String str) {
        this.this$0 = controlActionCoordinatorImpl;
        this.$controlId = str;
    }

    public final void run() {
        this.this$0.actionsInProgress.remove(this.$controlId);
    }
}
