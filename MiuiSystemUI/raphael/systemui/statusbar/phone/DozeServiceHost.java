package com.android.systemui.statusbar.phone;

import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.android.internal.annotations.VisibleForTesting;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.doze.DozeHost;
import com.android.systemui.doze.DozeLog;
import com.android.systemui.doze.DozeReceiver;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.statusbar.PulseExpansionHandler;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import dagger.Lazy;
import java.util.ArrayList;
import java.util.Iterator;

public class DozeServiceHost implements DozeHost {
    private View mAmbientIndicationContainer;
    private boolean mAnimateScreenOff;
    private boolean mAnimateWakeup;
    private final Lazy<AssistManager> mAssistManagerLazy;
    private final BatteryController mBatteryController;
    private final Lazy<BiometricUnlockController> mBiometricUnlockControllerLazy;
    private final ArrayList<DozeHost.Callback> mCallbacks = new ArrayList<>();
    private final DeviceProvisionedController mDeviceProvisionedController;
    private final DozeLog mDozeLog;
    private final DozeScrimController mDozeScrimController;
    protected boolean mDozingRequested;
    private final HeadsUpManagerPhone mHeadsUpManagerPhone;
    private boolean mIgnoreTouchWhilePulsing;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private final KeyguardViewMediator mKeyguardViewMediator;
    private NotificationIconAreaController mNotificationIconAreaController;
    private NotificationPanelViewController mNotificationPanel;
    private final NotificationShadeWindowController mNotificationShadeWindowController;
    private NotificationShadeWindowViewController mNotificationShadeWindowViewController;
    private final NotificationWakeUpCoordinator mNotificationWakeUpCoordinator;
    private Runnable mPendingScreenOffCallback;
    private final PowerManager mPowerManager;
    private final PulseExpansionHandler mPulseExpansionHandler;
    private boolean mPulsing;
    private final ScrimController mScrimController;
    private StatusBar mStatusBar;
    private StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private final SysuiStatusBarStateController mStatusBarStateController;
    private boolean mSuppressed;
    private final VisualStabilityManager mVisualStabilityManager;
    @VisibleForTesting
    boolean mWakeLockScreenPerformsAuth = SystemProperties.getBoolean("persist.sysui.wake_performs_auth", true);
    private WakefulnessLifecycle mWakefulnessLifecycle;

    public DozeServiceHost(DozeLog dozeLog, PowerManager powerManager, WakefulnessLifecycle wakefulnessLifecycle, SysuiStatusBarStateController sysuiStatusBarStateController, DeviceProvisionedController deviceProvisionedController, HeadsUpManagerPhone headsUpManagerPhone, BatteryController batteryController, ScrimController scrimController, Lazy<BiometricUnlockController> lazy, KeyguardViewMediator keyguardViewMediator, Lazy<AssistManager> lazy2, DozeScrimController dozeScrimController, KeyguardUpdateMonitor keyguardUpdateMonitor, VisualStabilityManager visualStabilityManager, PulseExpansionHandler pulseExpansionHandler, NotificationShadeWindowController notificationShadeWindowController, NotificationWakeUpCoordinator notificationWakeUpCoordinator, LockscreenLockIconController lockscreenLockIconController) {
        this.mDozeLog = dozeLog;
        this.mPowerManager = powerManager;
        this.mWakefulnessLifecycle = wakefulnessLifecycle;
        this.mStatusBarStateController = sysuiStatusBarStateController;
        this.mDeviceProvisionedController = deviceProvisionedController;
        this.mHeadsUpManagerPhone = headsUpManagerPhone;
        this.mBatteryController = batteryController;
        this.mScrimController = scrimController;
        this.mBiometricUnlockControllerLazy = lazy;
        this.mKeyguardViewMediator = keyguardViewMediator;
        this.mAssistManagerLazy = lazy2;
        this.mDozeScrimController = dozeScrimController;
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        this.mVisualStabilityManager = visualStabilityManager;
        this.mPulseExpansionHandler = pulseExpansionHandler;
        this.mNotificationShadeWindowController = notificationShadeWindowController;
        this.mNotificationWakeUpCoordinator = notificationWakeUpCoordinator;
    }

    public void initialize(StatusBar statusBar, NotificationIconAreaController notificationIconAreaController, StatusBarKeyguardViewManager statusBarKeyguardViewManager, NotificationShadeWindowViewController notificationShadeWindowViewController, NotificationPanelViewController notificationPanelViewController, View view) {
        this.mStatusBar = statusBar;
        this.mNotificationIconAreaController = notificationIconAreaController;
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
        this.mNotificationPanel = notificationPanelViewController;
        this.mNotificationShadeWindowViewController = notificationShadeWindowViewController;
        this.mAmbientIndicationContainer = view;
    }

    public String toString() {
        return "PSB.DozeServiceHost[mCallbacks=" + this.mCallbacks.size() + "]";
    }

    /* access modifiers changed from: package-private */
    public void firePowerSaveChanged(boolean z) {
        Iterator<DozeHost.Callback> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            it.next().onPowerSaveChanged(z);
        }
    }

    /* access modifiers changed from: package-private */
    public void fireNotificationPulse(NotificationEntry notificationEntry) {
        $$Lambda$DozeServiceHost$Xc4SX99X8IZoMaU0MD3jJJv7A3I r0 = new Runnable(notificationEntry) {
            /* class com.android.systemui.statusbar.phone.$$Lambda$DozeServiceHost$Xc4SX99X8IZoMaU0MD3jJJv7A3I */
            public final /* synthetic */ NotificationEntry f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                DozeServiceHost.this.lambda$fireNotificationPulse$0$DozeServiceHost(this.f$1);
            }
        };
        Iterator<DozeHost.Callback> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            it.next().onNotificationAlerted(r0);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$fireNotificationPulse$0 */
    public /* synthetic */ void lambda$fireNotificationPulse$0$DozeServiceHost(NotificationEntry notificationEntry) {
        notificationEntry.setPulseSuppressed(true);
        this.mNotificationIconAreaController.updateAodNotificationIcons();
    }

    /* access modifiers changed from: package-private */
    public boolean getDozingRequested() {
        return this.mDozingRequested;
    }

    /* access modifiers changed from: package-private */
    public boolean isPulsing() {
        return this.mPulsing;
    }

    @Override // com.android.systemui.doze.DozeHost
    public void addCallback(DozeHost.Callback callback) {
        this.mCallbacks.add(callback);
    }

    @Override // com.android.systemui.doze.DozeHost
    public void removeCallback(DozeHost.Callback callback) {
        this.mCallbacks.remove(callback);
    }

    @Override // com.android.systemui.doze.DozeHost
    public void startDozing() {
        if (!this.mDozingRequested) {
            this.mDozingRequested = true;
            updateDozing();
            this.mDozeLog.traceDozing(this.mStatusBarStateController.isDozing());
            this.mStatusBar.updateIsKeyguard();
            return;
        }
        this.mDozingRequested = true;
    }

    public void updateDozing() {
        int mode = this.mBiometricUnlockControllerLazy.get().getMode();
        boolean z = false;
        boolean z2 = (this.mDozingRequested && this.mStatusBarStateController.getState() == 1) || mode == 2;
        if (!(mode == 1 || mode == 7)) {
            z = z2;
        }
        this.mStatusBarStateController.setIsDozing(z);
    }

    @Override // com.android.systemui.doze.DozeHost
    public void pulseWhileDozing(final DozeHost.PulseCallback pulseCallback, int i) {
        if (i == 5) {
            this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), 4, "com.android.systemui:LONG_PRESS");
            this.mAssistManagerLazy.get().startAssist(new Bundle());
            return;
        }
        if (i == 8) {
            this.mScrimController.setWakeLockScreenSensorActive(true);
        }
        final boolean z = i == 8 && this.mWakeLockScreenPerformsAuth;
        this.mPulsing = true;
        this.mDozeScrimController.pulse(new DozeHost.PulseCallback() {
            /* class com.android.systemui.statusbar.phone.DozeServiceHost.AnonymousClass1 */

            @Override // com.android.systemui.doze.DozeHost.PulseCallback
            public void onPulseStarted() {
                pulseCallback.onPulseStarted();
                DozeServiceHost.this.mStatusBar.updateNotificationPanelTouchState();
                setPulsing(true);
            }

            @Override // com.android.systemui.doze.DozeHost.PulseCallback
            public void onPulseFinished() {
                DozeServiceHost.this.mPulsing = false;
                pulseCallback.onPulseFinished();
                DozeServiceHost.this.mStatusBar.updateNotificationPanelTouchState();
                DozeServiceHost.this.mScrimController.setWakeLockScreenSensorActive(false);
                setPulsing(false);
            }

            private void setPulsing(boolean z) {
                DozeServiceHost.this.mStatusBarStateController.setPulsing(z);
                DozeServiceHost.this.mStatusBarKeyguardViewManager.setPulsing(z);
                DozeServiceHost.this.mKeyguardViewMediator.setPulsing(z);
                DozeServiceHost.this.mNotificationPanel.setPulsing(z);
                DozeServiceHost.this.mVisualStabilityManager.setPulsing(z);
                DozeServiceHost.this.mIgnoreTouchWhilePulsing = false;
                if (DozeServiceHost.this.mKeyguardUpdateMonitor != null && z) {
                    DozeServiceHost.this.mKeyguardUpdateMonitor.onAuthInterruptDetected(z);
                }
                DozeServiceHost.this.mStatusBar.updateScrimController();
                DozeServiceHost.this.mPulseExpansionHandler.setPulsing(z);
                DozeServiceHost.this.mNotificationWakeUpCoordinator.setPulsing(z);
            }
        }, i);
        this.mStatusBar.updateScrimController();
    }

    @Override // com.android.systemui.doze.DozeHost
    public void stopDozing() {
        if (this.mDozingRequested) {
            this.mDozingRequested = false;
            updateDozing();
            this.mDozeLog.traceDozing(this.mStatusBarStateController.isDozing());
        }
    }

    @Override // com.android.systemui.doze.DozeHost
    public void onIgnoreTouchWhilePulsing(boolean z) {
        if (z != this.mIgnoreTouchWhilePulsing) {
            this.mDozeLog.tracePulseTouchDisabledByProx(z);
        }
        this.mIgnoreTouchWhilePulsing = z;
        if (this.mStatusBarStateController.isDozing() && z) {
            this.mNotificationShadeWindowViewController.cancelCurrentTouch();
        }
    }

    @Override // com.android.systemui.doze.DozeHost
    public void dozeTimeTick() {
        this.mNotificationPanel.dozeTimeTick();
        View view = this.mAmbientIndicationContainer;
        if (view instanceof DozeReceiver) {
            ((DozeReceiver) view).dozeTimeTick();
        }
    }

    @Override // com.android.systemui.doze.DozeHost
    public boolean isPowerSaveActive() {
        return this.mBatteryController.isAodPowerSave();
    }

    @Override // com.android.systemui.doze.DozeHost
    public boolean isPulsingBlocked() {
        return this.mBiometricUnlockControllerLazy.get().getMode() == 1;
    }

    @Override // com.android.systemui.doze.DozeHost
    public boolean isProvisioned() {
        return this.mDeviceProvisionedController.isDeviceProvisioned() && this.mDeviceProvisionedController.isCurrentUserSetup();
    }

    @Override // com.android.systemui.doze.DozeHost
    public boolean isBlockingDoze() {
        if (!this.mBiometricUnlockControllerLazy.get().hasPendingAuthentication()) {
            return false;
        }
        Log.i("StatusBar", "Blocking AOD because fingerprint has authenticated");
        return true;
    }

    @Override // com.android.systemui.doze.DozeHost
    public void extendPulse(int i) {
        if (i == 8) {
            this.mScrimController.setWakeLockScreenSensorActive(true);
        }
        if (!this.mDozeScrimController.isPulsing() || !this.mHeadsUpManagerPhone.hasNotifications()) {
            this.mDozeScrimController.extendPulse();
        } else {
            this.mHeadsUpManagerPhone.extendHeadsUp();
        }
    }

    @Override // com.android.systemui.doze.DozeHost
    public void setAnimateWakeup(boolean z) {
        if (this.mWakefulnessLifecycle.getWakefulness() != 2 && this.mWakefulnessLifecycle.getWakefulness() != 1) {
            this.mAnimateWakeup = z;
        }
    }

    @Override // com.android.systemui.doze.DozeHost
    public void setAnimateScreenOff(boolean z) {
        this.mAnimateScreenOff = z;
    }

    @Override // com.android.systemui.doze.DozeHost
    public void onSlpiTap(float f, float f2) {
        View view;
        if (f > 0.0f && f2 > 0.0f && (view = this.mAmbientIndicationContainer) != null && view.getVisibility() == 0) {
            int[] iArr = new int[2];
            this.mAmbientIndicationContainer.getLocationOnScreen(iArr);
            float f3 = f - ((float) iArr[0]);
            float f4 = f2 - ((float) iArr[1]);
            if (0.0f <= f3 && f3 <= ((float) this.mAmbientIndicationContainer.getWidth()) && 0.0f <= f4 && f4 <= ((float) this.mAmbientIndicationContainer.getHeight())) {
                long elapsedRealtime = SystemClock.elapsedRealtime();
                MotionEvent obtain = MotionEvent.obtain(elapsedRealtime, elapsedRealtime, 0, f, f2, 0);
                this.mAmbientIndicationContainer.dispatchTouchEvent(obtain);
                obtain.recycle();
                MotionEvent obtain2 = MotionEvent.obtain(elapsedRealtime, elapsedRealtime, 1, f, f2, 0);
                this.mAmbientIndicationContainer.dispatchTouchEvent(obtain2);
                obtain2.recycle();
            }
        }
    }

    @Override // com.android.systemui.doze.DozeHost
    public void setDozeScreenBrightness(int i) {
        this.mNotificationShadeWindowController.setDozeScreenBrightness(i);
    }

    @Override // com.android.systemui.doze.DozeHost
    public void setAodDimmingScrim(float f) {
        this.mScrimController.setAodFrontScrimAlpha(f);
    }

    @Override // com.android.systemui.doze.DozeHost
    public void prepareForGentleSleep(Runnable runnable) {
        if (this.mPendingScreenOffCallback != null) {
            Log.w("DozeServiceHost", "Overlapping onDisplayOffCallback. Ignoring previous one.");
        }
        this.mPendingScreenOffCallback = runnable;
        this.mStatusBar.updateScrimController();
    }

    @Override // com.android.systemui.doze.DozeHost
    public void cancelGentleSleep() {
        this.mPendingScreenOffCallback = null;
        if (this.mScrimController.getState() == ScrimState.OFF) {
            this.mStatusBar.updateScrimController();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasPendingScreenOffCallback() {
        return this.mPendingScreenOffCallback != null;
    }

    /* access modifiers changed from: package-private */
    public void executePendingScreenOffCallback() {
        Runnable runnable = this.mPendingScreenOffCallback;
        if (runnable != null) {
            runnable.run();
            this.mPendingScreenOffCallback = null;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean shouldAnimateWakeup() {
        return this.mAnimateWakeup;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldAnimateScreenOff() {
        return this.mAnimateScreenOff;
    }

    /* access modifiers changed from: package-private */
    public boolean getIgnoreTouchWhilePulsing() {
        return this.mIgnoreTouchWhilePulsing;
    }

    /* access modifiers changed from: package-private */
    public void setDozeSuppressed(boolean z) {
        if (z != this.mSuppressed) {
            this.mSuppressed = z;
            Iterator<DozeHost.Callback> it = this.mCallbacks.iterator();
            while (it.hasNext()) {
                it.next().onDozeSuppressedChanged(z);
            }
        }
    }

    @Override // com.android.systemui.doze.DozeHost
    public boolean isDozeSuppressed() {
        return this.mSuppressed;
    }
}
