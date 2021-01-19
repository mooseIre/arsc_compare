package com.android.systemui.statusbar.phone;

import android.view.View;
import com.android.systemui.plugins.qs.QS;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: MiuiNotificationPanelViewController.kt */
final class MiuiNotificationPanelViewController$refreshNssCoveringQs$2 implements Runnable {
    final /* synthetic */ MiuiNotificationPanelViewController this$0;

    MiuiNotificationPanelViewController$refreshNssCoveringQs$2(MiuiNotificationPanelViewController miuiNotificationPanelViewController) {
        this.this$0 = miuiNotificationPanelViewController;
    }

    public final void run() {
        if (this.this$0.mNssCoveredQs) {
            MiuiNotificationPanelViewController miuiNotificationPanelViewController = this.this$0;
            QS qs = miuiNotificationPanelViewController.mQs;
            Intrinsics.checkExpressionValueIsNotNull(qs, "mQs");
            View header = qs.getHeader();
            Intrinsics.checkExpressionValueIsNotNull(header, "mQs.header");
            miuiNotificationPanelViewController.updateScrollerTopPadding((float) header.getHeight());
            return;
        }
        MiuiNotificationPanelViewController miuiNotificationPanelViewController2 = this.this$0;
        miuiNotificationPanelViewController2.updateScrollerTopPadding(MiuiNotificationPanelViewController$refreshNssCoveringQs$2.super.calculateQsTopPadding());
    }
}
