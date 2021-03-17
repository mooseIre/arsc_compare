package com.android.systemui.statusbar.notification.interruption;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;

public interface NotificationInterruptSuppressor {
    default boolean suppressAwakeHeadsUp(NotificationEntry notificationEntry) {
        return false;
    }

    default boolean suppressAwakeInterruptions(NotificationEntry notificationEntry) {
        return false;
    }

    default boolean suppressInterruptions(NotificationEntry notificationEntry) {
        return false;
    }

    default String getName() {
        return getClass().getName();
    }
}
