package com.android.keyguard.injector;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.KeyguardBottomAreaView;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: KeyguardBottomAreaInjector.kt */
public final class KeyguardBottomAreaInjector extends MiuiKeyguardUpdateMonitorCallback implements IMiuiKeyguardWallpaperController.IWallpaperChangeCallback {
    @NotNull
    private final Context mContext;
    private KeyguardBottomAreaView mKeyguardBottomAreaView;
    private boolean mTouchAtKeyguardBottomArea;
    private float mTouchDownX;
    private float mTouchDownY;

    public KeyguardBottomAreaInjector(@NotNull Context context) {
        Intrinsics.checkParameterIsNotNull(context, "mContext");
        this.mContext = context;
    }

    @NotNull
    public final KeyguardBottomAreaView getView() {
        KeyguardBottomAreaView keyguardBottomAreaView = this.mKeyguardBottomAreaView;
        if (keyguardBottomAreaView != null) {
            return keyguardBottomAreaView;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mKeyguardBottomAreaView");
        throw null;
    }

    public final void setView(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "v");
        this.mKeyguardBottomAreaView = (KeyguardBottomAreaView) view;
    }

    public final void onAttachedToWindow() {
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this);
        ((IMiuiKeyguardWallpaperController) Dependency.get(IMiuiKeyguardWallpaperController.class)).registerWallpaperChangeCallback(this);
    }

    public final void onDetachedFromWindow() {
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).removeCallback(this);
        ((IMiuiKeyguardWallpaperController) Dependency.get(IMiuiKeyguardWallpaperController.class)).unregisterWallpaperChangeCallback(this);
    }

    public final boolean disallowInterceptTouch(@NotNull MotionEvent motionEvent) {
        boolean z;
        Intrinsics.checkParameterIsNotNull(motionEvent, "event");
        if (motionEvent.getActionMasked() == 0) {
            this.mTouchDownX = motionEvent.getX();
            this.mTouchDownY = motionEvent.getY();
            float y = motionEvent.getY();
            KeyguardBottomAreaView keyguardBottomAreaView = this.mKeyguardBottomAreaView;
            if (keyguardBottomAreaView != null) {
                if (y >= ((float) keyguardBottomAreaView.getTop())) {
                    KeyguardBottomAreaView keyguardBottomAreaView2 = this.mKeyguardBottomAreaView;
                    if (keyguardBottomAreaView2 == null) {
                        Intrinsics.throwUninitializedPropertyAccessException("mKeyguardBottomAreaView");
                        throw null;
                    } else if (keyguardBottomAreaView2.getVisibility() == 0) {
                        KeyguardBottomAreaView keyguardBottomAreaView3 = this.mKeyguardBottomAreaView;
                        if (keyguardBottomAreaView3 == null) {
                            Intrinsics.throwUninitializedPropertyAccessException("mKeyguardBottomAreaView");
                            throw null;
                        } else if (keyguardBottomAreaView3.getAlpha() == 1.0f) {
                            z = true;
                            this.mTouchAtKeyguardBottomArea = z;
                        }
                    }
                }
                z = false;
                this.mTouchAtKeyguardBottomArea = z;
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("mKeyguardBottomAreaView");
                throw null;
            }
        }
        return !this.mTouchAtKeyguardBottomArea || !isBottomAreaStartMoving(motionEvent);
    }

    private final boolean isBottomAreaStartMoving(MotionEvent motionEvent) {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(this.mContext);
        Intrinsics.checkExpressionValueIsNotNull(viewConfiguration, "ViewConfiguration.get(mContext)");
        int scaledTouchSlop = viewConfiguration.getScaledTouchSlop();
        if (this.mTouchAtKeyguardBottomArea) {
            float f = (float) scaledTouchSlop;
            if (Math.abs(motionEvent.getX() - this.mTouchDownX) >= f || Math.abs(motionEvent.getY() - this.mTouchDownY) >= f) {
                return true;
            }
        }
        return false;
    }

    @Override // com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController.IWallpaperChangeCallback
    public void onWallpaperChange(boolean z) {
        KeyguardBottomAreaView keyguardBottomAreaView = this.mKeyguardBottomAreaView;
        if (keyguardBottomAreaView != null) {
            keyguardBottomAreaView.setDarkStyle(z);
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("mKeyguardBottomAreaView");
            throw null;
        }
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onKeyguardBouncerChanged(boolean z) {
        if (z) {
            KeyguardBottomAreaView keyguardBottomAreaView = this.mKeyguardBottomAreaView;
            if (keyguardBottomAreaView != null) {
                keyguardBottomAreaView.cancelAnimations();
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("mKeyguardBottomAreaView");
                throw null;
            }
        }
    }

    public final void setAlpha(float f) {
        KeyguardBottomAreaView view = getView();
        if (view != null) {
            view.setAlpha(f);
        }
    }
}
