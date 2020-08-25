package com.android.systemui.statusbar;

import android.app.ITransientNotificationCallback;
import android.graphics.Rect;
import android.hardware.biometrics.IBiometricServiceReceiverInternal;
import android.os.Bundle;
import android.os.IBinder;
import com.android.internal.os.SomeArgs;
import com.android.internal.statusbar.IStatusBar;
import com.android.internal.view.AppearanceRegion;

public abstract class CompatibilityCommandQueue extends IStatusBar.Stub {
    public static int APPEARANCE_LIGHT_NAVIGATION_BARS = 16;
    public static int APPEARANCE_LIGHT_STATUS_BARS = 8;
    public static int APPEARANCE_LOW_PROFILE_BARS = 4;
    public static int APPEARANCE_OPAQUE_NAVIGATION_BARS = 2;
    public static int APPEARANCE_OPAQUE_STATUS_BARS = 1;
    private int mAppearance = 0;
    private Rect mDockedBounds = new Rect();
    private int mDockedStackVis = 0;
    private Rect mFullscreenBounds = new Rect();
    private int mFullscreenStackVis = 0;
    private boolean mIsFullScreen = false;
    private boolean mIsImmersive = false;
    private final Object mLock = new Object();
    private boolean mNavbarColorManagedByIme = false;
    private int mTransientTypes;

    private static boolean containBar(int i, int i2) {
        return (i & (1 << i2)) != 0;
    }

    private static int convertFlag(int i, int i2, int i3) {
        if ((i & i2) != 0) {
            return i3;
        }
        return 0;
    }

    private static int convertNoFlag(int i, int i2, int i3) {
        if ((i & i2) == 0) {
            return i3;
        }
        return 0;
    }

    public void dismissInattentiveSleepWarning(boolean z) {
    }

    public abstract void hideBiometricDialog();

    public abstract void onBiometricAuthenticated(boolean z, String str);

    public abstract void onBiometricError(SomeArgs someArgs);

    public void onDisplayReady(int i) {
    }

    public void onRecentsAnimationStateChanged(boolean z) {
    }

    public abstract void setSystemUiVisibility(int i, int i2, int i3, int i4, Rect rect, Rect rect2, boolean z);

    public abstract void showBiometricDialog(SomeArgs someArgs);

    public void showInattentiveSleepWarning() {
    }

    public abstract void showToast(SomeArgs someArgs);

    public void startTracing() {
    }

    public void stopTracing() {
    }

    public void showAuthenticationDialog(Bundle bundle, IBiometricServiceReceiverInternal iBiometricServiceReceiverInternal, int i, boolean z, int i2, String str, long j, int i3) {
        SomeArgs obtain = SomeArgs.obtain();
        obtain.arg1 = bundle;
        obtain.arg2 = iBiometricServiceReceiverInternal;
        obtain.argi1 = i;
        obtain.arg3 = Boolean.valueOf(z);
        obtain.argi2 = i2;
        obtain.arg4 = str;
        obtain.arg5 = Long.valueOf(j);
        obtain.argi3 = i3;
        showBiometricDialog(obtain);
    }

    public void onBiometricAuthenticated() {
        onBiometricAuthenticated(true, (String) null);
    }

    public void onBiometricError(int i, int i2, int i3) {
        SomeArgs obtain = SomeArgs.obtain();
        obtain.argi1 = i;
        obtain.argi2 = i2;
        obtain.argi3 = i3;
        onBiometricError(obtain);
    }

    public void hideAuthenticationDialog() {
        hideBiometricDialog();
    }

    public void onSystemBarAppearanceChanged(int i, int i2, AppearanceRegion[] appearanceRegionArr, boolean z) {
        int i3;
        if (i == 0) {
            synchronized (this.mLock) {
                this.mAppearance = i2;
                int i4 = 0;
                if (appearanceRegionArr.length > 0) {
                    int convertAppearanceToVisibility = convertAppearanceToVisibility(appearanceRegionArr[0].getAppearance());
                    this.mFullscreenBounds = appearanceRegionArr[0].getBounds();
                    i3 = convertAppearanceToVisibility;
                } else {
                    this.mFullscreenBounds.setEmpty();
                    i3 = 0;
                }
                if (appearanceRegionArr.length > 1) {
                    i4 = convertAppearanceToVisibility(appearanceRegionArr[1].getAppearance());
                    this.mDockedBounds = appearanceRegionArr[1].getBounds();
                } else {
                    this.mDockedBounds.setEmpty();
                }
                int i5 = i4;
                int combineVisibilityTransientFullScreenImmersive = combineVisibilityTransientFullScreenImmersive(i2, this.mTransientTypes, this.mIsFullScreen, this.mIsImmersive);
                this.mFullscreenStackVis = i3;
                this.mDockedStackVis = i5;
                this.mNavbarColorManagedByIme = z;
                setSystemUiVisibility(i, combineVisibilityTransientFullScreenImmersive, i3, i5, this.mFullscreenBounds, this.mDockedBounds, z);
            }
        }
    }

    public void showTransient(int i, int[] iArr) {
        if (i == 0) {
            synchronized (this.mLock) {
                for (int i2 : iArr) {
                    this.mTransientTypes = (1 << i2) | this.mTransientTypes;
                }
                setSystemUiVisibility(i, combineVisibilityTransientFullScreenImmersive(this.mAppearance, this.mTransientTypes, this.mIsFullScreen, this.mIsImmersive), this.mFullscreenStackVis, this.mDockedStackVis, this.mFullscreenBounds, this.mDockedBounds, this.mNavbarColorManagedByIme);
            }
        }
    }

    public void abortTransient(int i, int[] iArr) {
        if (i == 0) {
            synchronized (this.mLock) {
                for (int i2 : iArr) {
                    this.mTransientTypes = (~(1 << i2)) & this.mTransientTypes;
                }
                setSystemUiVisibility(i, combineVisibilityTransientFullScreenImmersive(this.mAppearance, this.mTransientTypes, this.mIsFullScreen, this.mIsImmersive), this.mFullscreenStackVis, this.mDockedStackVis, this.mFullscreenBounds, this.mDockedBounds, this.mNavbarColorManagedByIme);
            }
        }
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

    public void topAppWindowChanged(int i, boolean z, boolean z2) {
        if (i == 0) {
            synchronized (this.mLock) {
                this.mIsFullScreen = z;
                this.mIsImmersive = z2;
                setSystemUiVisibility(i, combineVisibilityTransientFullScreenImmersive(this.mAppearance, this.mTransientTypes, z, z2), this.mFullscreenStackVis, this.mDockedStackVis, this.mFullscreenBounds, this.mDockedBounds, this.mNavbarColorManagedByIme);
            }
        }
    }

    public static int convertAppearanceToVisibility(int i) {
        return convertFlag(i, APPEARANCE_LIGHT_NAVIGATION_BARS, 16) | convertNoFlag(i, APPEARANCE_OPAQUE_STATUS_BARS, 8) | 0 | convertNoFlag(i, APPEARANCE_OPAQUE_NAVIGATION_BARS, 32768) | convertFlag(i, APPEARANCE_LOW_PROFILE_BARS, 1) | convertFlag(i, APPEARANCE_LIGHT_STATUS_BARS, 8192);
    }

    private static int combineVisibilityTransientFullScreenImmersive(int i, int i2, boolean z, boolean z2) {
        int convertAppearanceToVisibility = convertAppearanceToVisibility(i);
        int i3 = containBar(i2, 0) ? convertAppearanceToVisibility | 67108864 : convertAppearanceToVisibility & -67108865;
        int i4 = containBar(i2, 1) ? i3 | 134217728 : i3 & -134217729;
        int i5 = z ? i4 | 6 : i4 & -7;
        return z2 ? i5 | 6144 : i5 & -6145;
    }
}
