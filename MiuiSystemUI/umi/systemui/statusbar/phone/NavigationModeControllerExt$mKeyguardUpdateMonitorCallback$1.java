package com.android.systemui.statusbar.phone;

import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;

/* compiled from: NavigationModeControllerExt.kt */
public final class NavigationModeControllerExt$mKeyguardUpdateMonitorCallback$1 extends MiuiKeyguardUpdateMonitorCallback {
    NavigationModeControllerExt$mKeyguardUpdateMonitorCallback$1() {
    }

    @Override // com.android.keyguard.MiuiKeyguardUpdateMonitorCallback
    public void onKeyguardShowingChanged(boolean z) {
        NavigationBarView defaultNavigationBarView;
        ButtonDispatcher recentsButton;
        if (!NavigationModeControllerExt.INSTANCE.getMIsFsgMode() && (defaultNavigationBarView = NavigationModeControllerExt.INSTANCE.getNavigationBarController().getDefaultNavigationBarView()) != null && (recentsButton = defaultNavigationBarView.getRecentsButton()) != null) {
            recentsButton.setAlpha(z ? 0.0f : 1.0f);
        }
    }
}
