package com.android.systemui.statusbar.phone;

/* access modifiers changed from: package-private */
/* compiled from: MiuiNotificationPanelViewController.kt */
public final class MiuiNotificationPanelViewController$startWakeupAnimation$1 implements Runnable {
    final /* synthetic */ float $translationY;
    final /* synthetic */ MiuiNotificationPanelViewController this$0;

    MiuiNotificationPanelViewController$startWakeupAnimation$1(MiuiNotificationPanelViewController miuiNotificationPanelViewController, float f) {
        this.this$0 = miuiNotificationPanelViewController;
        this.$translationY = f;
    }

    public final void run() {
        this.this$0.startNotificationWakeAnimation(this.$translationY);
    }
}
