package com.android.systemui.statusbar.phone;

import android.hardware.Sensor;
import android.hardware.TriggerEventListener;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.Dumpable;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.util.sensors.AsyncSensorManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: KeyguardLiftController.kt */
public final class KeyguardLiftController extends KeyguardUpdateMonitorCallback implements StatusBarStateController.StateListener, Dumpable {
    private final AsyncSensorManager asyncSensorManager;
    private boolean bouncerVisible;
    private boolean isListening;
    private final KeyguardUpdateMonitor keyguardUpdateMonitor;
    private final TriggerEventListener listener = new KeyguardLiftController$listener$1(this);
    private final Sensor pickupSensor;
    private final StatusBarStateController statusBarStateController;

    public KeyguardLiftController(@NotNull StatusBarStateController statusBarStateController2, @NotNull AsyncSensorManager asyncSensorManager2, @NotNull KeyguardUpdateMonitor keyguardUpdateMonitor2, @NotNull DumpManager dumpManager) {
        Intrinsics.checkParameterIsNotNull(statusBarStateController2, "statusBarStateController");
        Intrinsics.checkParameterIsNotNull(asyncSensorManager2, "asyncSensorManager");
        Intrinsics.checkParameterIsNotNull(keyguardUpdateMonitor2, "keyguardUpdateMonitor");
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        this.statusBarStateController = statusBarStateController2;
        this.asyncSensorManager = asyncSensorManager2;
        this.keyguardUpdateMonitor = keyguardUpdateMonitor2;
        this.pickupSensor = asyncSensorManager2.getDefaultSensor(25);
        String name = KeyguardLiftController.class.getName();
        Intrinsics.checkExpressionValueIsNotNull(name, "javaClass.name");
        dumpManager.registerDumpable(name, this);
        this.statusBarStateController.addCallback(this);
        this.keyguardUpdateMonitor.registerCallback(this);
        updateListeningState();
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozingChanged(boolean z) {
        updateListeningState();
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onKeyguardBouncerChanged(boolean z) {
        this.bouncerVisible = z;
        updateListeningState();
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onKeyguardVisibilityChanged(boolean z) {
        updateListeningState();
    }

    @Override // com.android.systemui.Dumpable
    public void dump(@NotNull FileDescriptor fileDescriptor, @NotNull PrintWriter printWriter, @NotNull String[] strArr) {
        Intrinsics.checkParameterIsNotNull(fileDescriptor, "fd");
        Intrinsics.checkParameterIsNotNull(printWriter, "pw");
        Intrinsics.checkParameterIsNotNull(strArr, "args");
        printWriter.println("KeyguardLiftController:");
        printWriter.println("  pickupSensor: " + this.pickupSensor);
        printWriter.println("  isListening: " + this.isListening);
        printWriter.println("  bouncerVisible: " + this.bouncerVisible);
    }

    /* access modifiers changed from: private */
    public final void updateListeningState() {
        if (this.pickupSensor != null) {
            boolean z = true;
            if (!(this.keyguardUpdateMonitor.isKeyguardVisible() && !this.statusBarStateController.isDozing()) && !this.bouncerVisible) {
                z = false;
            }
            if (z != this.isListening) {
                this.isListening = z;
                if (z) {
                    this.asyncSensorManager.requestTriggerSensor(this.listener, this.pickupSensor);
                } else {
                    this.asyncSensorManager.cancelTriggerSensor(this.listener, this.pickupSensor);
                }
            }
        }
    }
}
