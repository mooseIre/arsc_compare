package com.android.keyguard.injector;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.SystemClock;
import android.util.Slog;
import android.view.Display;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: KeyguardSensorInjector.kt */
public final class KeyguardSensorInjector$mPickupSensorListener$1 implements SensorEventListener {
    final /* synthetic */ KeyguardSensorInjector this$0;

    public void onAccuracyChanged(@NotNull Sensor sensor, int i) {
        Intrinsics.checkParameterIsNotNull(sensor, "sensor");
    }

    KeyguardSensorInjector$mPickupSensorListener$1(KeyguardSensorInjector keyguardSensorInjector) {
        this.this$0 = keyguardSensorInjector;
    }

    public void onSensorChanged(@NotNull SensorEvent sensorEvent) {
        Intrinsics.checkParameterIsNotNull(sensorEvent, "event");
        float[] fArr = sensorEvent.values;
        if (fArr == null || fArr[0] != 1.0f) {
            float[] fArr2 = sensorEvent.values;
            if (fArr2 == null) {
                return;
            }
            if ((fArr2[0] == 2.0f || fArr2[0] == 0.0f) && this.this$0.mWakeupByPickUp && this.this$0.getMKeyguardViewMediator().isShowingAndNotOccluded()) {
                String tag = this.this$0.getTAG();
                Slog.i(tag, this.this$0.SCREEN_OFF_REASON + ":put down");
                this.this$0.getMPowerManager().goToSleep(SystemClock.uptimeMillis());
                Display access$getMDisplay$p = this.this$0.mDisplay;
                if (access$getMDisplay$p != null) {
                    int state = access$getMDisplay$p.getState();
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            }
        } else {
            Display access$getMDisplay$p2 = this.this$0.mDisplay;
            if (access$getMDisplay$p2 != null) {
                if (access$getMDisplay$p2.getState() != 2) {
                    this.this$0.mWakeupByPickUp = true;
                }
                this.this$0.getMPowerManager().wakeUp(SystemClock.uptimeMillis(), "com.android.systemui:PICK_UP");
                String tag2 = this.this$0.getTAG();
                Slog.i(tag2, this.this$0.SCREEN_OPEN_REASON + ":pick up");
                return;
            }
            Intrinsics.throwNpe();
            throw null;
        }
    }
}
