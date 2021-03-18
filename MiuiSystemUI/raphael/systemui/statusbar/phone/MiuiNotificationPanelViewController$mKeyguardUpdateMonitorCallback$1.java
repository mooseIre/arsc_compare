package com.android.systemui.statusbar.phone;

import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;

/* compiled from: MiuiNotificationPanelViewController.kt */
public final class MiuiNotificationPanelViewController$mKeyguardUpdateMonitorCallback$1 extends MiuiKeyguardUpdateMonitorCallback {
    final /* synthetic */ MiuiNotificationPanelViewController this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MiuiNotificationPanelViewController$mKeyguardUpdateMonitorCallback$1(MiuiNotificationPanelViewController miuiNotificationPanelViewController) {
        this.this$0 = miuiNotificationPanelViewController;
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onKeyguardBouncerChanged(boolean z) {
        this.this$0.onBouncerShowingChanged(z);
    }

    @Override // com.android.keyguard.MiuiKeyguardUpdateMonitorCallback
    public void onKeyguardShowingChanged(boolean z) {
        if (z) {
            this.this$0.addAwesomeLockScreenIfNeed();
        } else {
            this.this$0.removeAwesomeLockScreen();
        }
    }

    @Override // com.android.keyguard.MiuiKeyguardUpdateMonitorCallback
    public void onKeyguardOccludedChanged(boolean z) {
        this.this$0.mIsKeyguardOccluded = z;
    }
}
