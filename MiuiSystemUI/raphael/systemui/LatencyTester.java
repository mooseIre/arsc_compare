package com.android.systemui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.biometrics.BiometricSourceType;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import com.android.internal.util.LatencyTracker;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.phone.BiometricUnlockController;

public class LatencyTester extends SystemUI {
    private final BiometricUnlockController mBiometricUnlockController;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final PowerManager mPowerManager;

    public LatencyTester(Context context, BiometricUnlockController biometricUnlockController, PowerManager powerManager, BroadcastDispatcher broadcastDispatcher) {
        super(context);
        this.mBiometricUnlockController = biometricUnlockController;
        this.mPowerManager = powerManager;
        this.mBroadcastDispatcher = broadcastDispatcher;
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        if (Build.IS_DEBUGGABLE) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.android.systemui.latency.ACTION_FINGERPRINT_WAKE");
            intentFilter.addAction("com.android.systemui.latency.ACTION_TURN_ON_SCREEN");
            this.mBroadcastDispatcher.registerReceiver(new BroadcastReceiver() {
                /* class com.android.systemui.LatencyTester.AnonymousClass1 */

                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if ("com.android.systemui.latency.ACTION_FINGERPRINT_WAKE".equals(action)) {
                        LatencyTester.this.fakeWakeAndUnlock();
                    } else if ("com.android.systemui.latency.ACTION_TURN_ON_SCREEN".equals(action)) {
                        LatencyTester.this.fakeTurnOnScreen();
                    }
                }
            }, intentFilter);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void fakeTurnOnScreen() {
        if (LatencyTracker.isEnabled(this.mContext)) {
            LatencyTracker.getInstance(this.mContext).onActionStart(5);
        }
        this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), 0, "android.policy:LATENCY_TESTS");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void fakeWakeAndUnlock() {
        this.mBiometricUnlockController.onBiometricAcquired(BiometricSourceType.FINGERPRINT);
        this.mBiometricUnlockController.onBiometricAuthenticated(KeyguardUpdateMonitor.getCurrentUser(), BiometricSourceType.FINGERPRINT, true);
    }
}
