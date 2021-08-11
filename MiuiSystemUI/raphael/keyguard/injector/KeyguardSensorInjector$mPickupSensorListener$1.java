package com.android.keyguard.injector;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.SystemClock;
import android.util.Slog;
import android.view.Display;
import com.android.keyguard.wallpaper.MiuiKeyguardWallpaperControllerImpl;
import com.android.systemui.Dependency;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: KeyguardSensorInjector.kt */
public final class KeyguardSensorInjector$mPickupSensorListener$1 implements SensorEventListener {
    final /* synthetic */ KeyguardSensorInjector this$0;

    public void onAccuracyChanged(@NotNull Sensor sensor, int i) {
        Intrinsics.checkParameterIsNotNull(sensor, "sensor");
    }

    /* JADX WARN: Incorrect args count in method signature: ()V */
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
            if ((fArr2[0] == 2.0f || fArr2[0] == 0.0f) && KeyguardSensorInjector.access$getMWakeupByPickUp$p(this.this$0) && this.this$0.getMKeyguardViewMediator().isShowingAndNotOccluded()) {
                String tag = this.this$0.getTAG();
                Slog.i(tag, KeyguardSensorInjector.access$getSCREEN_OFF_REASON$p(this.this$0) + ":put down");
                this.this$0.getMPowerManager().goToSleep(SystemClock.uptimeMillis());
                Display access$getMDisplay$p = KeyguardSensorInjector.access$getMDisplay$p(this.this$0);
                if (access$getMDisplay$p != null) {
                    access$getMDisplay$p.getState();
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            }
        } else {
            Display access$getMDisplay$p2 = KeyguardSensorInjector.access$getMDisplay$p(this.this$0);
            if (access$getMDisplay$p2 == null) {
                Intrinsics.throwNpe();
                throw null;
            } else if (access$getMDisplay$p2.getState() != 2 || !((MiuiKeyguardWallpaperControllerImpl) Dependency.get(MiuiKeyguardWallpaperControllerImpl.class)).isAodUsingSuperWallpaper()) {
                this.this$0.getMPowerManager().wakeUp(SystemClock.uptimeMillis(), "com.android.systemui:PICK_UP");
                KeyguardSensorInjector.access$setMWakeupByPickUp$p(this.this$0, true);
                String tag2 = this.this$0.getTAG();
                Slog.i(tag2, KeyguardSensorInjector.access$getSCREEN_OPEN_REASON$p(this.this$0) + ":pick up");
            }
        }
    }
}
