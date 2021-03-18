package com.android.systemui.statusbar.phone;

/* access modifiers changed from: package-private */
/* compiled from: MiuiNotificationPanelViewController.kt */
public final class MiuiNotificationPanelViewController$mHidePanelRunnable$1 implements Runnable {
    final /* synthetic */ MiuiNotificationPanelViewController this$0;

    MiuiNotificationPanelViewController$mHidePanelRunnable$1(MiuiNotificationPanelViewController miuiNotificationPanelViewController) {
        this.this$0 = miuiNotificationPanelViewController;
    }

    public final void run() {
        this.this$0.instantCollapse();
    }
}
