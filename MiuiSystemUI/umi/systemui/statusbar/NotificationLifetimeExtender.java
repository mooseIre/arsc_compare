package com.android.systemui.statusbar;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;

public interface NotificationLifetimeExtender {

    public interface NotificationSafeToRemoveCallback {
        void onSafeToRemove(String str);
    }

    void setCallback(NotificationSafeToRemoveCallback notificationSafeToRemoveCallback);

    void setShouldManageLifetime(NotificationEntry notificationEntry, boolean z);

    boolean shouldExtendLifetime(NotificationEntry notificationEntry);

    boolean shouldExtendLifetimeForPendingNotification(NotificationEntry notificationEntry) {
        return false;
    }
}
