package com.android.systemui.keyguard;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.Trace;
import android.util.Log;
import android.util.Slog;
import com.android.internal.policy.IKeyguardDismissCallback;
import com.android.internal.policy.IKeyguardDrawnCallback;
import com.android.internal.policy.IKeyguardExitCallback;
import com.android.internal.policy.IKeyguardService;
import com.android.internal.policy.IKeyguardStateCallback;
import com.android.systemui.SystemUIApplication;

public class KeyguardService extends Service {
    private final IKeyguardService.Stub mBinder = new IKeyguardService.Stub() {
        /* class com.android.systemui.keyguard.KeyguardService.AnonymousClass1 */

        public void OnDoubleClickHome() {
        }

        public void addStateMonitorCallback(IKeyguardStateCallback iKeyguardStateCallback) {
            KeyguardService.this.checkPermission();
            KeyguardService.this.mKeyguardViewMediator.addStateMonitorCallback(iKeyguardStateCallback);
        }

        public void verifyUnlock(IKeyguardExitCallback iKeyguardExitCallback) {
            Trace.beginSection("KeyguardService.mBinder#verifyUnlock");
            KeyguardService.this.checkPermission();
            KeyguardService.this.mKeyguardViewMediator.verifyUnlock(iKeyguardExitCallback);
            Trace.endSection();
        }

        public void setOccluded(boolean z, boolean z2) {
            Trace.beginSection("KeyguardService.mBinder#setOccluded");
            KeyguardService.this.checkPermission();
            KeyguardService.this.mKeyguardViewMediator.setOccluded(z, z2);
            Trace.endSection();
        }

        public void dismiss(IKeyguardDismissCallback iKeyguardDismissCallback, CharSequence charSequence) {
            KeyguardService.this.checkPermission();
            KeyguardService.this.mKeyguardViewMediator.dismiss(iKeyguardDismissCallback, charSequence);
        }

        public void onDreamingStarted() {
            KeyguardService.this.checkPermission();
            KeyguardService.this.mKeyguardViewMediator.onDreamingStarted();
        }

        public void onDreamingStopped() {
            KeyguardService.this.checkPermission();
            KeyguardService.this.mKeyguardViewMediator.onDreamingStopped();
        }

        public void onStartedGoingToSleep(int i) {
            KeyguardService.this.checkPermission();
            KeyguardService.this.mKeyguardViewMediator.onStartedGoingToSleep(i);
            KeyguardService.this.mKeyguardLifecyclesDispatcher.dispatch(6);
        }

        public void onFinishedGoingToSleep(int i, boolean z) {
            KeyguardService.this.checkPermission();
            KeyguardService.this.mKeyguardViewMediator.onFinishedGoingToSleep(i, z);
            KeyguardService.this.mKeyguardLifecyclesDispatcher.dispatch(7);
        }

        public void onStartedWakingUp() {
            Trace.beginSection("KeyguardService.mBinder#onStartedWakingUp");
            KeyguardService.this.checkPermission();
            KeyguardService.this.mKeyguardViewMediator.onStartedWakingUp();
            KeyguardService.this.mKeyguardLifecyclesDispatcher.dispatch(4);
            Trace.endSection();
        }

        public void onStartedWakingUp(String str) {
            Trace.beginSection("KeyguardService.mBinder#onStartedWakingUpWithReason");
            Slog.d("miui_face", "receive waking up with reason");
            KeyguardService.this.checkPermission();
            KeyguardService.this.mKeyguardViewMediator.onStartedWakingUp(str);
            Trace.endSection();
        }

        public void onFinishedWakingUp() {
            Trace.beginSection("KeyguardService.mBinder#onFinishedWakingUp");
            KeyguardService.this.checkPermission();
            KeyguardService.this.mKeyguardLifecyclesDispatcher.dispatch(5);
            Trace.endSection();
        }

        public void onScreenTurningOn(IKeyguardDrawnCallback iKeyguardDrawnCallback) {
            Trace.beginSection("KeyguardService.mBinder#onScreenTurningOn");
            KeyguardService.this.checkPermission();
            KeyguardService.this.mKeyguardViewMediator.onScreenTurningOn(iKeyguardDrawnCallback);
            KeyguardService.this.mKeyguardLifecyclesDispatcher.dispatch(0);
            Trace.endSection();
        }

        public void onScreenTurnedOn() {
            Trace.beginSection("KeyguardService.mBinder#onScreenTurnedOn");
            KeyguardService.this.checkPermission();
            KeyguardService.this.mKeyguardViewMediator.onScreenTurnedOn();
            KeyguardService.this.mKeyguardLifecyclesDispatcher.dispatch(1);
            Trace.endSection();
        }

        public void onScreenTurningOff() {
            KeyguardService.this.checkPermission();
            KeyguardService.this.mKeyguardLifecyclesDispatcher.dispatch(2);
        }

        public void onScreenTurnedOff() {
            KeyguardService.this.checkPermission();
            KeyguardService.this.mKeyguardViewMediator.onScreenTurnedOff();
            KeyguardService.this.mKeyguardLifecyclesDispatcher.dispatch(3);
        }

        public void setKeyguardEnabled(boolean z) {
            KeyguardService.this.checkPermission();
            KeyguardService.this.mKeyguardViewMediator.setKeyguardEnabled(z);
        }

        public void onSystemReady() {
            Trace.beginSection("KeyguardService.mBinder#onSystemReady");
            KeyguardService.this.checkPermission();
            KeyguardService.this.mKeyguardViewMediator.onSystemReady();
            Trace.endSection();
        }

        public void doKeyguardTimeout(Bundle bundle) {
            KeyguardService.this.checkPermission();
            KeyguardService.this.mKeyguardViewMediator.doKeyguardTimeout(bundle);
        }

        public void setSwitchingUser(boolean z) {
            KeyguardService.this.checkPermission();
            KeyguardService.this.mKeyguardViewMediator.setSwitchingUser(z);
        }

        public void setCurrentUser(int i) {
            KeyguardService.this.checkPermission();
            KeyguardService.this.mKeyguardViewMediator.setCurrentUser(i);
        }

        public void onBootCompleted() {
            KeyguardService.this.checkPermission();
            KeyguardService.this.mKeyguardViewMediator.onBootCompleted();
        }

        public void startKeyguardExitAnimation(long j, long j2) {
            Trace.beginSection("KeyguardService.mBinder#startKeyguardExitAnimation");
            KeyguardService.this.checkPermission();
            KeyguardService.this.mKeyguardViewMediator.startKeyguardExitAnimation(j, j2);
            Trace.endSection();
        }

        public void onShortPowerPressedGoHome() {
            KeyguardService.this.checkPermission();
            KeyguardService.this.mKeyguardViewMediator.onShortPowerPressedGoHome();
        }

        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i != 255) {
                return KeyguardService.super.onTransact(i, parcel, parcel2, i2);
            }
            parcel.enforceInterface("com.android.internal.policy.IKeyguardService");
            onStartedWakingUp(parcel.readString());
            parcel2.writeNoException();
            return true;
        }
    };
    private final KeyguardLifecyclesDispatcher mKeyguardLifecyclesDispatcher;
    private final KeyguardViewMediator mKeyguardViewMediator;

    public KeyguardService(KeyguardViewMediator keyguardViewMediator, KeyguardLifecyclesDispatcher keyguardLifecyclesDispatcher) {
        this.mKeyguardViewMediator = keyguardViewMediator;
        this.mKeyguardLifecyclesDispatcher = keyguardLifecyclesDispatcher;
    }

    public void onCreate() {
        ((SystemUIApplication) getApplication()).startServicesIfNeeded();
    }

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    /* access modifiers changed from: package-private */
    public void checkPermission() {
        if (Binder.getCallingUid() != 1000 && getBaseContext().checkCallingOrSelfPermission("android.permission.CONTROL_KEYGUARD") != 0) {
            Log.w("KeyguardService", "Caller needs permission 'android.permission.CONTROL_KEYGUARD' to call " + Debug.getCaller());
            throw new SecurityException("Access denied to process: " + Binder.getCallingPid() + ", must have permission " + "android.permission.CONTROL_KEYGUARD");
        }
    }
}
