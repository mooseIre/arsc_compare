package com.android.systemui.statusbar.notification.collection;

import android.app.Notification;
import android.service.notification.StatusBarNotification;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotificationRankingManager.kt */
public final class NotificationRankingManagerKt {
    /* access modifiers changed from: private */
    public static final boolean isSystemMax(@NotNull NotificationEntry notificationEntry) {
        if (notificationEntry.getImportance() >= 4) {
            ExpandedNotification sbn = notificationEntry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn, "sbn");
            if (isSystemNotification(sbn)) {
                return true;
            }
        }
        return false;
    }

    private static final boolean isSystemNotification(@NotNull StatusBarNotification statusBarNotification) {
        return Intrinsics.areEqual((Object) "android", (Object) statusBarNotification.getPackageName()) || Intrinsics.areEqual((Object) "com.android.systemui", (Object) statusBarNotification.getPackageName());
    }

    /* access modifiers changed from: private */
    public static final boolean isColorizedForegroundService(@NotNull NotificationEntry notificationEntry) {
        ExpandedNotification sbn = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn, "sbn");
        Notification notification = sbn.getNotification();
        return notification.isForegroundService() && notification.isColorized() && notificationEntry.getImportance() > 1;
    }
}
