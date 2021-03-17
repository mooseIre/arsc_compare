package com.android.systemui.statusbar.phone;

/* compiled from: MiuiNotificationPanelViewController.kt */
final class MiuiNotificationPanelViewController$refreshNssCoveringQs$2 implements Runnable {
    final /* synthetic */ MiuiNotificationPanelViewController this$0;

    MiuiNotificationPanelViewController$refreshNssCoveringQs$2(MiuiNotificationPanelViewController miuiNotificationPanelViewController) {
        this.this$0 = miuiNotificationPanelViewController;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0008, code lost:
        r0 = r2.this$0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void run() {
        /*
            r2 = this;
            com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController r0 = r2.this$0
            boolean r0 = r0.mNssCoveredQs
            if (r0 == 0) goto L_0x0025
            com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController r0 = r2.this$0
            com.android.systemui.plugins.qs.QS r1 = r0.mQs
            if (r1 == 0) goto L_0x0025
            java.lang.String r2 = "mQs"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r1, r2)
            android.view.View r2 = r1.getHeader()
            java.lang.String r1 = "mQs.header"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r2, r1)
            int r2 = r2.getHeight()
            float r2 = (float) r2
            r0.updateScrollerTopPadding(r2)
            goto L_0x002e
        L_0x0025:
            com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController r2 = r2.this$0
            float r0 = com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController$refreshNssCoveringQs$2.super.calculateQsTopPadding()
            r2.updateScrollerTopPadding(r0)
        L_0x002e:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController$refreshNssCoveringQs$2.run():void");
    }
}
