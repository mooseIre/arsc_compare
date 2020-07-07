package com.android.systemui.statusbar;

import android.hardware.biometrics.IBiometricServiceReceiverInternal;
import android.os.Bundle;
import com.android.internal.os.SomeArgs;
import com.android.internal.statusbar.IStatusBar;

public abstract class CompatibilityCommandQueue extends IStatusBar.Stub {
    public void onDisplayReady(int i) {
    }

    public void onRecentsAnimationStateChanged(boolean z) {
    }

    public abstract void showBiometricDialog(SomeArgs someArgs);

    public void showBiometricDialog(Bundle bundle, IBiometricServiceReceiverInternal iBiometricServiceReceiverInternal, int i, boolean z, int i2) {
        SomeArgs obtain = SomeArgs.obtain();
        obtain.arg1 = bundle;
        obtain.arg2 = iBiometricServiceReceiverInternal;
        obtain.argi1 = i;
        obtain.arg3 = Boolean.valueOf(z);
        obtain.argi2 = i2;
        showBiometricDialog(obtain);
    }
}
