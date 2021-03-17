package com.android.keyguard.injector;

/* compiled from: KeyguardSensorInjector.kt */
final class KeyguardSensorInjector$unregisterLargeAreaTouchSensor$1 implements Runnable {
    final /* synthetic */ KeyguardSensorInjector this$0;

    KeyguardSensorInjector$unregisterLargeAreaTouchSensor$1(KeyguardSensorInjector keyguardSensorInjector) {
        this.this$0 = keyguardSensorInjector;
    }

    public final void run() {
        if (this.this$0.mSensorManager != null && this.this$0.mLargeAreaTouchSensor != null) {
            this.this$0.mLargeAreaTouchSensor = null;
            this.this$0.mSensorManager.unregisterListener(this.this$0.mLargeAreaTouchSensorListener);
        }
    }
}
