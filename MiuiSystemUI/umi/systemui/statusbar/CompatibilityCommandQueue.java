package com.android.systemui.statusbar;

import android.app.ITransientNotificationCallback;
import android.hardware.biometrics.IBiometricServiceReceiverInternal;
import android.os.Bundle;
import android.os.IBinder;
import com.android.internal.os.SomeArgs;
import com.android.internal.statusbar.IStatusBar;
import com.android.internal.view.AppearanceRegion;

public abstract class CompatibilityCommandQueue extends IStatusBar.Stub {
    public void abortTransient(int i, int[] iArr) {
    }

    public void dismissInattentiveSleepWarning(boolean z) {
    }

    public void hideAuthenticationDialog() {
    }

    public void onBiometricAuthenticated() {
    }

    public void onBiometricError(int i, int i2, int i3) {
    }

    public void onDisplayReady(int i) {
    }

    public void onRecentsAnimationStateChanged(boolean z) {
    }

    public void onSystemBarAppearanceChanged(int i, int i2, AppearanceRegion[] appearanceRegionArr, boolean z) {
    }

    public abstract void showBiometricDialog(SomeArgs someArgs);

    public void showInattentiveSleepWarning() {
    }

    public abstract void showToast(SomeArgs someArgs);

    public void showTransient(int i, int[] iArr) {
    }

    public void startTracing() {
    }

    public void stopTracing() {
    }

    public void suppressAmbientDisplay(boolean z) {
    }

    public void showAuthenticationDialog(Bundle bundle, IBiometricServiceReceiverInternal iBiometricServiceReceiverInternal, int i, boolean z, int i2, String str, long j, int i3) {
        SomeArgs obtain = SomeArgs.obtain();
        obtain.arg1 = bundle;
        obtain.arg2 = iBiometricServiceReceiverInternal;
        obtain.argi1 = i;
        obtain.arg3 = Boolean.valueOf(z);
        obtain.argi2 = i2;
        showBiometricDialog(obtain);
    }

    public void showToast(int i, String str, IBinder iBinder, CharSequence charSequence, IBinder iBinder2, int i2, ITransientNotificationCallback iTransientNotificationCallback) {
        SomeArgs obtain = SomeArgs.obtain();
        obtain.arg1 = str;
        obtain.arg2 = iBinder;
        obtain.arg3 = charSequence;
        obtain.arg4 = iBinder2;
        obtain.arg5 = iTransientNotificationCallback;
        obtain.argi1 = i;
        obtain.argi2 = i2;
        showToast(obtain);
    }
}
