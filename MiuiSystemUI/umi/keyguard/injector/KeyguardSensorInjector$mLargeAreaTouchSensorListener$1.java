package com.android.keyguard.injector;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.SystemClock;
import android.util.Slog;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.systemui.Dependency;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: KeyguardSensorInjector.kt */
public final class KeyguardSensorInjector$mLargeAreaTouchSensorListener$1 implements SensorEventListener {
    final /* synthetic */ KeyguardSensorInjector this$0;

    public void onAccuracyChanged(@NotNull Sensor sensor, int i) {
        Intrinsics.checkParameterIsNotNull(sensor, "sensor");
    }

    /* JADX WARN: Incorrect args count in method signature: ()V */
    KeyguardSensorInjector$mLargeAreaTouchSensorListener$1(KeyguardSensorInjector keyguardSensorInjector) {
        this.this$0 = keyguardSensorInjector;
    }

    public void onSensorChanged(@NotNull SensorEvent sensorEvent) {
        Intrinsics.checkParameterIsNotNull(sensorEvent, "event");
        float[] fArr = sensorEvent.values;
        if (fArr != null && fArr[0] == 1.0f && !this.this$0.getMKeyguardViewMediator().isHiding() && this.this$0.getMKeyguardViewMediator().isShowing() && !((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).isFingerprintUnlock()) {
            if (!this.this$0.getMKeyguardViewMediator().isOccluded() || !MiuiKeyguardUtils.keepScreenOnWhenLargeAreaTouch(this.this$0.getMContext())) {
                String tag = this.this$0.getTAG();
                Slog.i(tag, this.this$0.SCREEN_OFF_REASON + ":large area touch");
                this.this$0.getMPowerManager().goToSleep(SystemClock.uptimeMillis());
            }
        }
    }
}
