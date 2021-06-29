package com.android.keyguard.injector;

import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.injector.KeyguardSensorInjector;
import kotlin.jvm.internal.Intrinsics;
import miui.util.ProximitySensorWrapper;

/* access modifiers changed from: package-private */
/* compiled from: KeyguardSensorInjector.kt */
public final class KeyguardSensorInjector$mProximitySensorListener$1 implements ProximitySensorWrapper.ProximitySensorChangeListener {
    final /* synthetic */ KeyguardSensorInjector this$0;

    KeyguardSensorInjector$mProximitySensorListener$1(KeyguardSensorInjector keyguardSensorInjector) {
        this.this$0 = keyguardSensorInjector;
    }

    public final void onSensorChanged(boolean z) {
        if (this.this$0.mProximitySensorChangeCallback != null) {
            AnalyticsHelper.getInstance(this.this$0.getMContext()).recordKeyguardProximitySensor(z);
            KeyguardSensorInjector.ProximitySensorChangeCallback proximitySensorChangeCallback = this.this$0.mProximitySensorChangeCallback;
            if (proximitySensorChangeCallback != null) {
                proximitySensorChangeCallback.onChange(z);
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        }
        this.this$0.unregisterProximitySensor();
    }
}
