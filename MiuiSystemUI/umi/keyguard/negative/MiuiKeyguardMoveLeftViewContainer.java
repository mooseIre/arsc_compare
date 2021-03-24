package com.android.keyguard.negative;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.keyguard.injector.KeyguardNegative1PageInjector;
import com.android.keyguard.magazine.LockScreenMagazineController;
import com.android.systemui.C0017R$layout;
import com.android.systemui.Dependency;

public class MiuiKeyguardMoveLeftViewContainer extends FrameLayout {
    MiuiKeyguardMoveLeftBaseView mKeyguardMoveLeftView;

    public MiuiKeyguardMoveLeftViewContainer(Context context) {
        this(context, null);
    }

    public MiuiKeyguardMoveLeftViewContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void inflateLeftView() {
        MiuiKeyguardMoveLeftBaseView miuiKeyguardMoveLeftBaseView = this.mKeyguardMoveLeftView;
        if (miuiKeyguardMoveLeftBaseView != null) {
            removeView(miuiKeyguardMoveLeftBaseView);
            this.mKeyguardMoveLeftView = null;
        }
        if (((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isSupportLockScreenMagazineLeft()) {
            MiuiKeyguardMoveLeftLockScreenMagazineView miuiKeyguardMoveLeftLockScreenMagazineView = (MiuiKeyguardMoveLeftLockScreenMagazineView) LayoutInflater.from(getContext()).inflate(C0017R$layout.miui_keyguard_left_view_lock_screen_magazine_layout, (ViewGroup) null, false);
            this.mKeyguardMoveLeftView = miuiKeyguardMoveLeftLockScreenMagazineView;
            miuiKeyguardMoveLeftLockScreenMagazineView.setVisibility(4);
        } else {
            MiuiKeyguardMoveLeftControlCenterView miuiKeyguardMoveLeftControlCenterView = (MiuiKeyguardMoveLeftControlCenterView) LayoutInflater.from(getContext()).inflate(C0017R$layout.miui_keyguard_left_view_control_center_layout, (ViewGroup) null, false);
            this.mKeyguardMoveLeftView = miuiKeyguardMoveLeftControlCenterView;
            miuiKeyguardMoveLeftControlCenterView.setVisibility(0);
        }
        setCustomBackground();
        addView(this.mKeyguardMoveLeftView);
    }

    public void removeLeftView() {
        MiuiKeyguardMoveLeftBaseView miuiKeyguardMoveLeftBaseView = this.mKeyguardMoveLeftView;
        if (miuiKeyguardMoveLeftBaseView != null) {
            removeView(miuiKeyguardMoveLeftBaseView);
        }
    }

    public void initLeftView() {
        MiuiKeyguardMoveLeftBaseView miuiKeyguardMoveLeftBaseView = this.mKeyguardMoveLeftView;
        if (miuiKeyguardMoveLeftBaseView != null) {
            miuiKeyguardMoveLeftBaseView.initLeftView();
        }
    }

    public void uploadData() {
        MiuiKeyguardMoveLeftBaseView miuiKeyguardMoveLeftBaseView = this.mKeyguardMoveLeftView;
        if (miuiKeyguardMoveLeftBaseView != null) {
            miuiKeyguardMoveLeftBaseView.uploadData();
        }
    }

    public boolean isSupportRightMove() {
        MiuiKeyguardMoveLeftBaseView miuiKeyguardMoveLeftBaseView = this.mKeyguardMoveLeftView;
        if (miuiKeyguardMoveLeftBaseView == null) {
            return false;
        }
        return miuiKeyguardMoveLeftBaseView.isSupportRightMove();
    }

    public void setCustomBackground(Drawable drawable) {
        MiuiKeyguardMoveLeftBaseView miuiKeyguardMoveLeftBaseView = this.mKeyguardMoveLeftView;
        if (miuiKeyguardMoveLeftBaseView != null) {
            miuiKeyguardMoveLeftBaseView.setCustomBackground(drawable);
        }
    }

    public void setCustomBackground() {
        MiuiKeyguardMoveLeftBaseView miuiKeyguardMoveLeftBaseView = this.mKeyguardMoveLeftView;
        if (miuiKeyguardMoveLeftBaseView != null) {
            miuiKeyguardMoveLeftBaseView.setCustomBackground(null);
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((KeyguardNegative1PageInjector) Dependency.get(KeyguardNegative1PageInjector.class)).onAttachedToWindow();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((KeyguardNegative1PageInjector) Dependency.get(KeyguardNegative1PageInjector.class)).onDetachedFromWindow();
    }
}
