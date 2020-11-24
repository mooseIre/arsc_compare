package com.android.keyguard.injector;

import com.android.keyguard.KeyguardUpdateMonitorCallback;

/* compiled from: KeyguardSensorInjector.kt */
public final class KeyguardSensorInjector$mKeyguardUpdateMonitorCallback$1 extends KeyguardUpdateMonitorCallback {
    final /* synthetic */ KeyguardSensorInjector this$0;

    KeyguardSensorInjector$mKeyguardUpdateMonitorCallback$1(KeyguardSensorInjector keyguardSensorInjector) {
        this.this$0 = keyguardSensorInjector;
    }

    public void onKeyguardBouncerChanged(boolean z) {
        if (this.this$0.getMKeyguardUpdateMonitor().isKeyguardVisible()) {
            this.this$0.disableFullScreenGesture();
        }
    }

    public void onKeyguardVisibilityChanged(boolean z) {
        if (!z) {
            this.this$0.unregisterPickupSensor();
            this.this$0.unregisterProximitySensor();
            this.this$0.unregisterLargeAreaTouchSensor();
        }
    }
}
