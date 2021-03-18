package com.android.systemui.statusbar.notification.collection;

import android.app.Notification;
import android.service.notification.StatusBarNotification;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import kotlin.jvm.internal.Intrinsics;

public final class NotificationRankingManagerKt {
    public static final boolean isSystemMax(NotificationEntry notificationEntry) {
        if (notificationEntry.getImportance() >= 4) {
            ExpandedNotification sbn = notificationEntry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn, "sbn");
            if (isSystemNotification(sbn)) {
                return true;
            }
        }
        return false;
    }

    private static final boolean isSystemNotification(StatusBarNotification statusBarNotification) {
        return Intrinsics.areEqual("android", statusBarNotification.getPackageName()) || Intrinsics.areEqual("com.android.systemui", statusBarNotification.getPackageName());
    }

    public static final boolean isColorizedForegroundService(NotificationEntry notificationEntry) {
        ExpandedNotification sbn = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn, "sbn");
        Notification notification = sbn.getNotification();
        return notification.isForegroundService() && notification.isColorized() && notificationEntry.getImportance() > 1;
    }
}
