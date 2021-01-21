package com.android.systemui.statusbar.phone;

import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;

/* compiled from: MiuiNotificationPanelViewController.kt */
public final class MiuiNotificationPanelViewController$mKeyguardUpdateMonitorCallback$1 extends MiuiKeyguardUpdateMonitorCallback {
    final /* synthetic */ MiuiNotificationPanelViewController this$0;

    MiuiNotificationPanelViewController$mKeyguardUpdateMonitorCallback$1(MiuiNotificationPanelViewController miuiNotificationPanelViewController) {
        this.this$0 = miuiNotificationPanelViewController;
    }

    public void onKeyguardBouncerChanged(boolean z) {
        this.this$0.onBouncerShowingChanged(z);
    }

    public void onKeyguardShowingChanged(boolean z) {
        if (z) {
            this.this$0.addAwesomeLockScreenIfNeed();
        } else {
            this.this$0.removeAwesomeLockScreen();
        }
    }
}
