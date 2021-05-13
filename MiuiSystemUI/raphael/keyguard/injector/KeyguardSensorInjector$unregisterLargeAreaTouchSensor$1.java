package com.android.keyguard.injector;

/* access modifiers changed from: package-private */
/* compiled from: KeyguardSensorInjector.kt */
public final class KeyguardSensorInjector$unregisterLargeAreaTouchSensor$1 implements Runnable {
    final /* synthetic */ KeyguardSensorInjector this$0;

    KeyguardSensorInjector$unregisterLargeAreaTouchSensor$1(KeyguardSensorInjector keyguardSensorInjector) {
        this.this$0 = keyguardSensorInjector;
    }

    public final void run() {
        if (KeyguardSensorInjector.access$getMSensorManager$p(this.this$0) != null && KeyguardSensorInjector.access$getMLargeAreaTouchSensor$p(this.this$0) != null) {
            KeyguardSensorInjector.access$setMLargeAreaTouchSensor$p(this.this$0, null);
            KeyguardSensorInjector.access$getMSensorManager$p(this.this$0).unregisterListener(KeyguardSensorInjector.access$getMLargeAreaTouchSensorListener$p(this.this$0));
        }
    }
}
