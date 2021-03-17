package com.android.systemui.statusbar.notification.row;

import android.app.Notification;
import android.app.PendingIntent;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiExpandableNotificationRow.kt */
public final class MiuiExpandableNotificationRowKt {
    /* access modifiers changed from: private */
    public static final PendingIntent getPendingIntent(@Nullable Notification notification) {
        PendingIntent pendingIntent;
        if (notification != null && (pendingIntent = notification.contentIntent) != null) {
            return pendingIntent;
        }
        if (notification != null) {
            return notification.fullScreenIntent;
        }
        return null;
    }
}
