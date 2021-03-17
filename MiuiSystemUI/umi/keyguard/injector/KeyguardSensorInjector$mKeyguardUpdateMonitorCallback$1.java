package com.android.keyguard.injector;

import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;

/* compiled from: KeyguardSensorInjector.kt */
public final class KeyguardSensorInjector$mKeyguardUpdateMonitorCallback$1 extends MiuiKeyguardUpdateMonitorCallback {
    final /* synthetic */ KeyguardSensorInjector this$0;

    KeyguardSensorInjector$mKeyguardUpdateMonitorCallback$1(KeyguardSensorInjector keyguardSensorInjector) {
        this.this$0 = keyguardSensorInjector;
    }

    public void onKeyguardBouncerChanged(boolean z) {
        this.this$0.disableFullScreenGesture();
    }

    public void onKeyguardVisibilityChanged(boolean z) {
        if (!z) {
            this.this$0.unregisterPickupSensor();
            this.this$0.unregisterProximitySensor();
        }
    }

    public void onKeyguardShowingChanged(boolean z) {
        if (!z) {
            this.this$0.unregisterLargeAreaTouchSensor();
        }
    }
}
