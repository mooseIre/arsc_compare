package com.android.systemui.statusbar.notification.stack;

import java.util.ArrayList;

/* compiled from: NotificationStackScrollLayoutExt.kt */
final class NotificationStackScrollLayoutExtKt$clearUnimportantNotifications$1 implements Runnable {
    final /* synthetic */ NotificationStackScrollLayout $this_clearUnimportantNotifications;
    final /* synthetic */ ArrayList $viewToRemove;

    NotificationStackScrollLayoutExtKt$clearUnimportantNotifications$1(NotificationStackScrollLayout notificationStackScrollLayout, ArrayList arrayList) {
        this.$this_clearUnimportantNotifications = notificationStackScrollLayout;
        this.$viewToRemove = arrayList;
    }

    public final void run() {
        this.$this_clearUnimportantNotifications.lambda$clearNotifications$9(this.$viewToRemove, 2);
    }
}
