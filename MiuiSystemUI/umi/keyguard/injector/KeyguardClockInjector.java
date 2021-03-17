package com.android.keyguard.injector;

import android.content.Context;
import com.android.keyguard.clock.KeyguardClockContainer;
import com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController;
import com.android.systemui.Dependency;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: KeyguardClockInjector.kt */
public final class KeyguardClockInjector implements IMiuiKeyguardWallpaperController.IWallpaperChangeCallback {
    private KeyguardClockContainer mKeyguardClockView;

    public KeyguardClockInjector(@NotNull Context context) {
        Intrinsics.checkParameterIsNotNull(context, "mContext");
    }

    public final void onFinishInflate(@NotNull KeyguardClockContainer keyguardClockContainer) {
        Intrinsics.checkParameterIsNotNull(keyguardClockContainer, "view");
        this.mKeyguardClockView = keyguardClockContainer;
    }

    @NotNull
    public final KeyguardClockContainer getView() {
        KeyguardClockContainer keyguardClockContainer = this.mKeyguardClockView;
        if (keyguardClockContainer != null) {
            return keyguardClockContainer;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mKeyguardClockView");
        throw null;
    }

    public final void onAttachedToWindow() {
        ((IMiuiKeyguardWallpaperController) Dependency.get(IMiuiKeyguardWallpaperController.class)).registerWallpaperChangeCallback(this);
    }

    public final void onDetachedFromWindow() {
        ((IMiuiKeyguardWallpaperController) Dependency.get(IMiuiKeyguardWallpaperController.class)).unregisterWallpaperChangeCallback(this);
    }

    @Override // com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController.IWallpaperChangeCallback
    public void onWallpaperChange(boolean z) {
        KeyguardClockContainer keyguardClockContainer = this.mKeyguardClockView;
        if (keyguardClockContainer != null) {
            keyguardClockContainer.setDarkStyle(z);
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("mKeyguardClockView");
            throw null;
        }
    }

    public final void setAlpha(float f) {
        getView().setAlpha(f);
    }
}
