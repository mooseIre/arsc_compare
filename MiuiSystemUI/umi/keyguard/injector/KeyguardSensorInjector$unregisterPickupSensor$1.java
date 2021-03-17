package com.android.keyguard.injector;

import android.hardware.SensorManager;
import android.util.Slog;

/* access modifiers changed from: package-private */
/* compiled from: KeyguardSensorInjector.kt */
public final class KeyguardSensorInjector$unregisterPickupSensor$1 implements Runnable {
    final /* synthetic */ KeyguardSensorInjector this$0;

    KeyguardSensorInjector$unregisterPickupSensor$1(KeyguardSensorInjector keyguardSensorInjector) {
        this.this$0 = keyguardSensorInjector;
    }

    public final void run() {
        Slog.i(this.this$0.getTAG(), "unregister pickup sensor");
        this.this$0.mWakeupAndSleepSensor = null;
        SensorManager sensorManager = this.this$0.mSensorManager;
        if (sensorManager != null) {
            sensorManager.unregisterListener(this.this$0.mPickupSensorListener);
        }
    }
}
