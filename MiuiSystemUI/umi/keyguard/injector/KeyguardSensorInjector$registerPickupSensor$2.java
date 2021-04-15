package com.android.keyguard.injector;

import android.util.Slog;

/* access modifiers changed from: package-private */
/* compiled from: KeyguardSensorInjector.kt */
public final class KeyguardSensorInjector$registerPickupSensor$2 implements Runnable {
    final /* synthetic */ KeyguardSensorInjector this$0;

    KeyguardSensorInjector$registerPickupSensor$2(KeyguardSensorInjector keyguardSensorInjector) {
        this.this$0 = keyguardSensorInjector;
    }

    public final void run() {
        if (this.this$0.mWakeupAndSleepSensor != null) {
            Slog.i(this.this$0.getTAG(), "register pickup sensor");
            this.this$0.mSensorManager.registerListener(this.this$0.mPickupSensorListener, this.this$0.mWakeupAndSleepSensor, 3);
        }
    }
}
