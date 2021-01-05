package com.android.systemui.statusbar.phone;

import com.android.keyguard.KeyguardUpdateMonitorCallback;

/* compiled from: MiuiNotificationPanelViewController.kt */
public final class MiuiNotificationPanelViewController$mKeyguardUpdateMonitorCallback$1 extends KeyguardUpdateMonitorCallback {
    final /* synthetic */ MiuiNotificationPanelViewController this$0;

    MiuiNotificationPanelViewController$mKeyguardUpdateMonitorCallback$1(MiuiNotificationPanelViewController miuiNotificationPanelViewController) {
        this.this$0 = miuiNotificationPanelViewController;
    }

    public void onKeyguardBouncerChanged(boolean z) {
        this.this$0.onBouncerShowingChanged(z);
    }
}
