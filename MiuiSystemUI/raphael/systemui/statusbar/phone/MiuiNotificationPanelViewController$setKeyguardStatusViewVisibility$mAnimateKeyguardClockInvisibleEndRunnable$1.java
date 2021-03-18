package com.android.systemui.statusbar.phone;

import kotlin.jvm.internal.Ref$ObjectRef;

/* compiled from: MiuiNotificationPanelViewController.kt */
final class MiuiNotificationPanelViewController$setKeyguardStatusViewVisibility$mAnimateKeyguardClockInvisibleEndRunnable$1 implements Runnable {
    final /* synthetic */ Ref$ObjectRef $keyguardClockView;
    final /* synthetic */ MiuiNotificationPanelViewController this$0;

    MiuiNotificationPanelViewController$setKeyguardStatusViewVisibility$mAnimateKeyguardClockInvisibleEndRunnable$1(MiuiNotificationPanelViewController miuiNotificationPanelViewController, Ref$ObjectRef ref$ObjectRef) {
        this.this$0 = miuiNotificationPanelViewController;
        this.$keyguardClockView = ref$ObjectRef;
    }

    public final void run() {
        this.this$0.mKeyguardStatusViewAnimating = false;
        this.$keyguardClockView.element.setVisibility(4);
    }
}
