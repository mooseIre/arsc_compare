package com.android.keyguard.injector;

import android.util.Slog;

/* compiled from: KeyguardSensorInjector.kt */
final class KeyguardSensorInjector$registerPickupSensor$2 implements Runnable {
    final /* synthetic */ KeyguardSensorInjector this$0;

    KeyguardSensorInjector$registerPickupSensor$2(KeyguardSensorInjector keyguardSensorInjector) {
        this.this$0 = keyguardSensorInjector;
    }

    public final void run() {
        if (KeyguardSensorInjector.access$getMWakeupAndSleepSensor$p(this.this$0) != null) {
            Slog.i(this.this$0.getTAG(), "register pickup sensor");
            KeyguardSensorInjector.access$getMSensorManager$p(this.this$0).registerListener(KeyguardSensorInjector.access$getMPickupSensorListener$p(this.this$0), KeyguardSensorInjector.access$getMWakeupAndSleepSensor$p(this.this$0), 3);
        }
    }
}
