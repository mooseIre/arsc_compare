package com.android.systemui.statusbar.phone;

import android.view.View;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Lambda;

/* compiled from: MiuiNotificationPanelViewController.kt */
final class MiuiNotificationPanelViewController$startWakeupAnimation$animateShadeViews$2 extends Lambda implements Function1<View, Boolean> {
    public static final MiuiNotificationPanelViewController$startWakeupAnimation$animateShadeViews$2 INSTANCE = new MiuiNotificationPanelViewController$startWakeupAnimation$animateShadeViews$2();

    MiuiNotificationPanelViewController$startWakeupAnimation$animateShadeViews$2() {
        super(1);
    }

    public /* bridge */ /* synthetic */ Object invoke(Object obj) {
        return Boolean.valueOf(invoke((View) obj));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0004, code lost:
        r0 = ((com.android.systemui.statusbar.notification.row.ExpandableView) r1).getViewState();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final boolean invoke(android.view.View r1) {
        /*
            r0 = this;
            boolean r0 = r1 instanceof com.android.systemui.statusbar.notification.row.ExpandableView
            if (r0 == 0) goto L_0x0012
            com.android.systemui.statusbar.notification.row.ExpandableView r1 = (com.android.systemui.statusbar.notification.row.ExpandableView) r1
            com.android.systemui.statusbar.notification.stack.ExpandableViewState r0 = r1.getViewState()
            if (r0 == 0) goto L_0x0012
            boolean r0 = r0.hidden
            if (r0 != 0) goto L_0x0012
            r0 = 1
            goto L_0x0013
        L_0x0012:
            r0 = 0
        L_0x0013:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController$startWakeupAnimation$animateShadeViews$2.invoke(android.view.View):boolean");
    }
}
