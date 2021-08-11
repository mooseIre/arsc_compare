package com.android.keyguard.injector;

/* compiled from: KeyguardSensorInjector.kt */
final class KeyguardSensorInjector$registerLargeAreaTouchSensor$1 implements Runnable {
    final /* synthetic */ KeyguardSensorInjector this$0;

    KeyguardSensorInjector$registerLargeAreaTouchSensor$1(KeyguardSensorInjector keyguardSensorInjector) {
        this.this$0 = keyguardSensorInjector;
    }

    public final void run() {
        if (KeyguardSensorInjector.access$shouldRegisterLargeAreaSensor(this.this$0)) {
            KeyguardSensorInjector keyguardSensorInjector = this.this$0;
            KeyguardSensorInjector.access$setMLargeAreaTouchSensor$p(keyguardSensorInjector, KeyguardSensorInjector.access$getMSensorManager$p(keyguardSensorInjector).getDefaultSensor(KeyguardSensorInjector.access$getLARGE_AREA_TOUCH_SENSOR$p(this.this$0)));
            KeyguardSensorInjector.access$getMSensorManager$p(this.this$0).registerListener(KeyguardSensorInjector.access$getMLargeAreaTouchSensorListener$p(this.this$0), KeyguardSensorInjector.access$getMLargeAreaTouchSensor$p(this.this$0), 3);
        }
    }
}
