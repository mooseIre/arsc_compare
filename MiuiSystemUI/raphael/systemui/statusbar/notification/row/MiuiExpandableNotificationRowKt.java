package com.android.systemui.statusbar.notification.row;

import android.app.Notification;
import android.app.PendingIntent;

public final class MiuiExpandableNotificationRowKt {
    public static final PendingIntent getPendingIntent(Notification notification) {
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
