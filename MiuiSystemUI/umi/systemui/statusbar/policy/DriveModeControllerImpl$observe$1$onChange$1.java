package com.android.systemui.statusbar.policy;

/* compiled from: DriveModeControllerImpl.kt */
final class DriveModeControllerImpl$observe$1$onChange$1 implements Runnable {
    final /* synthetic */ DriveModeControllerImpl$observe$1 this$0;

    DriveModeControllerImpl$observe$1$onChange$1(DriveModeControllerImpl$observe$1 driveModeControllerImpl$observe$1) {
        this.this$0 = driveModeControllerImpl$observe$1;
    }

    public final void run() {
        this.this$0.this$0.updateDriveModeValue();
        this.this$0.this$0.dispatchOnDriveModeChanged();
    }
}
