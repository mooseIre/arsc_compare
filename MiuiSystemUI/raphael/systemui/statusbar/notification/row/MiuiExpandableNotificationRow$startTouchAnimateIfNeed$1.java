package com.android.systemui.statusbar.notification.row;

/* access modifiers changed from: package-private */
/* compiled from: MiuiExpandableNotificationRow.kt */
public final class MiuiExpandableNotificationRow$startTouchAnimateIfNeed$1 implements Runnable {
    final /* synthetic */ MiuiExpandableNotificationRow this$0;

    MiuiExpandableNotificationRow$startTouchAnimateIfNeed$1(MiuiExpandableNotificationRow miuiExpandableNotificationRow) {
        this.this$0 = miuiExpandableNotificationRow;
    }

    public final void run() {
        this.this$0.startTouchScaleAnimateIfNeed(1.0f);
    }
}
