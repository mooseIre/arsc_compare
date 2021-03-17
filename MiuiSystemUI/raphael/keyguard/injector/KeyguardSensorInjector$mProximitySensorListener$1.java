package com.android.keyguard.injector;

import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.injector.KeyguardSensorInjector;
import kotlin.jvm.internal.Intrinsics;
import miui.util.ProximitySensorWrapper;

/* compiled from: KeyguardSensorInjector.kt */
final class KeyguardSensorInjector$mProximitySensorListener$1 implements ProximitySensorWrapper.ProximitySensorChangeListener {
    final /* synthetic */ KeyguardSensorInjector this$0;

    KeyguardSensorInjector$mProximitySensorListener$1(KeyguardSensorInjector keyguardSensorInjector) {
        this.this$0 = keyguardSensorInjector;
    }

    public final void onSensorChanged(boolean z) {
        if (this.this$0.mProximitySensorChangeCallback != null) {
            AnalyticsHelper.getInstance(this.this$0.getMContext()).recordKeyguardProximitySensor(z);
            KeyguardSensorInjector.ProximitySensorChangeCallback access$getMProximitySensorChangeCallback$p = this.this$0.mProximitySensorChangeCallback;
            if (access$getMProximitySensorChangeCallback$p != null) {
                access$getMProximitySensorChangeCallback$p.onChange(z);
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        }
        this.this$0.unregisterProximitySensor();
    }
}
