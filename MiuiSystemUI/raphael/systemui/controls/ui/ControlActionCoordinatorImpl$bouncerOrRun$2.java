package com.android.systemui.controls.ui;

/* access modifiers changed from: package-private */
/* compiled from: ControlActionCoordinatorImpl.kt */
public final class ControlActionCoordinatorImpl$bouncerOrRun$2 implements Runnable {
    final /* synthetic */ ControlActionCoordinatorImpl this$0;

    ControlActionCoordinatorImpl$bouncerOrRun$2(ControlActionCoordinatorImpl controlActionCoordinatorImpl) {
        this.this$0 = controlActionCoordinatorImpl;
    }

    public final void run() {
        this.this$0.pendingAction = null;
    }
}
