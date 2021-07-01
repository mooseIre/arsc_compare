package com.android.keyguard.injector;

/* compiled from: KeyguardSensorInjector.kt */
final class KeyguardSensorInjector$mUnregisterProximitySensorRunnable$1 implements Runnable {
    final /* synthetic */ KeyguardSensorInjector this$0;

    KeyguardSensorInjector$mUnregisterProximitySensorRunnable$1(KeyguardSensorInjector keyguardSensorInjector) {
        this.this$0 = keyguardSensorInjector;
    }

    public final void run() {
        this.this$0.unregisterProximitySensor();
    }
}
