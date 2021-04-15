package com.android.keyguard.injector;

/* access modifiers changed from: package-private */
/* compiled from: KeyguardSensorInjector.kt */
public final class KeyguardSensorInjector$registerLargeAreaTouchSensor$1 implements Runnable {
    final /* synthetic */ KeyguardSensorInjector this$0;

    KeyguardSensorInjector$registerLargeAreaTouchSensor$1(KeyguardSensorInjector keyguardSensorInjector) {
        this.this$0 = keyguardSensorInjector;
    }

    public final void run() {
        if (this.this$0.shouldRegisterLargeAreaSensor()) {
            KeyguardSensorInjector keyguardSensorInjector = this.this$0;
            keyguardSensorInjector.mLargeAreaTouchSensor = keyguardSensorInjector.mSensorManager.getDefaultSensor(this.this$0.LARGE_AREA_TOUCH_SENSOR);
            this.this$0.mSensorManager.registerListener(this.this$0.mLargeAreaTouchSensorListener, this.this$0.mLargeAreaTouchSensor, 3);
        }
    }
}
