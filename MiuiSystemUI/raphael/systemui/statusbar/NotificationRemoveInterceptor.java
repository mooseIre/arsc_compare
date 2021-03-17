package com.android.systemui.statusbar;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;

public interface NotificationRemoveInterceptor {
    boolean onNotificationRemoveRequested(String str, NotificationEntry notificationEntry, int i);
}
